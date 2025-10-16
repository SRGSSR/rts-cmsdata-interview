package ch.rts.interview.gateway;

import ch.rts.interview.persistence.Article;
import ch.rts.interview.persistence.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleRecommendationService {

	private final ArticleRepository articleRepository;
	private final ArticleAnalysisService analysisService;

	public ArticleRecommendationService(ArticleRepository articleRepository, 
	                                   ArticleAnalysisService analysisService) {
		this.articleRepository = articleRepository;
		this.analysisService = analysisService;
	}

	public List<Article> getTopStories(int count) {
		return analysisService.getInterestingArticles(count);
	}

	public List<Article> getDailyDigest() {
		return analysisService.getInterestingArticlesDiversified(10);
	}

	public List<Article> getTrendingBySection(String section, int count) {
		List<Article> allArticles = articleRepository.findAll();
		
		return allArticles.stream()
				.filter(article -> hasSection(article, section))
				.filter(article -> isRecent(article, 30))
				.sorted(Comparator.comparing(Article::getPublicationDate).reversed())
				.limit(count)
				.collect(Collectors.toList());
	}

	public Map<String, List<Article>> getHighlightsByCategory(int articlesPerCategory) {
		List<Article> allArticles = articleRepository.findAll();
		
		Map<String, List<Article>> categoryMap = new HashMap<>();
		Set<String> mainCategories = Set.of("Sport", "Culture", "Actualités", "Économie");
		
		for (String category : mainCategories) {
			List<Article> categoryArticles = allArticles.stream()
					.filter(article -> hasSection(article, category))
					.sorted(Comparator.comparing(Article::getPublicationDate).reversed())
					.limit(articlesPerCategory)
					.collect(Collectors.toList());
			
			if (!categoryArticles.isEmpty()) {
				categoryMap.put(category, categoryArticles);
			}
		}
		
		return categoryMap;
	}

	public List<Article> getRecentBreakingNews(int hours) {
		LocalDateTime cutoffTime = LocalDateTime.now().minusHours(hours);
		List<Article> allArticles = articleRepository.findAll();
		
		return allArticles.stream()
				.filter(article -> article.getPublicationDate().isAfter(cutoffTime))
				.filter(article -> hasSection(article, "Actualités"))
				.sorted(Comparator.comparing(Article::getPublicationDate).reversed())
				.collect(Collectors.toList());
	}

	public List<Article> getSimilarArticles(Long articleId, int count) {
		Optional<Article> targetArticle = articleRepository.findById(articleId);
		
		if (targetArticle.isEmpty()) {
			return Collections.emptyList();
		}
		
		Article target = targetArticle.get();
		List<Article> allArticles = articleRepository.findAll();
		
		return allArticles.stream()
				.filter(article -> !article.getId().equals(articleId))
				.map(article -> new SimilarityScore(article, calculateSimilarity(target, article)))
				.filter(score -> score.similarity > 0)
				.sorted(Comparator.comparingDouble(SimilarityScore::similarity).reversed())
				.limit(count)
				.map(SimilarityScore::article)
				.collect(Collectors.toList());
	}

	private double calculateSimilarity(Article article1, Article article2) {
		double similarity = 0.0;
		
		// Section overlap
		if (article1.getSections() != null && article2.getSections() != null) {
			Set<String> sections1 = new HashSet<>(Arrays.asList(article1.getSections()));
			Set<String> sections2 = new HashSet<>(Arrays.asList(article2.getSections()));
			
			Set<String> intersection = new HashSet<>(sections1);
			intersection.retainAll(sections2);
			
			Set<String> union = new HashSet<>(sections1);
			union.addAll(sections2);
			
			if (!union.isEmpty()) {
				similarity += (double) intersection.size() / union.size() * 70;
			}
		}
		
		// Time proximity
		long daysDifference = Math.abs(
				java.time.temporal.ChronoUnit.DAYS.between(
						article1.getPublicationDate(), 
						article2.getPublicationDate()
				)
		);
		
		if (daysDifference <= 7) {
			similarity += 30 - (daysDifference * 4);
		}
		
		return similarity;
	}

	private boolean hasSection(Article article, String section) {
		if (article.getSections() == null) {
			return false;
		}
		return Arrays.asList(article.getSections()).contains(section);
	}

	private boolean isRecent(Article article, int days) {
		LocalDateTime cutoff = LocalDateTime.now().minusDays(days);
		return article.getPublicationDate().isAfter(cutoff);
	}

	private record SimilarityScore(Article article, double similarity) {}
}

