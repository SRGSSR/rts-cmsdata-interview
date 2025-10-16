package ch.rts.interview.gateway;

import ch.rts.interview.persistence.Article;
import ch.rts.interview.persistence.ArticleRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class ArticleAnalysisService {

	private final ArticleRepository articleRepository;

	public ArticleAnalysisService(ArticleRepository articleRepository) {
		this.articleRepository = articleRepository;
	}

	public List<Article> getInterestingArticles(int limit) {
		List<Article> allArticles = articleRepository.findAll();
		
		return allArticles.stream()
				.map(article -> new ScoredArticle(article, calculateInterestScore(article)))
				.sorted(Comparator.comparingDouble(ScoredArticle::score).reversed())
				.limit(limit)
				.map(ScoredArticle::article)
				.collect(Collectors.toList());
	}

	public List<Article> getInterestingArticlesDiversified(int limit) {
		List<Article> allArticles = articleRepository.findAll();
		
		Map<String, List<Article>> articlesBySection = groupByPrimarySection(allArticles);
		
		List<Article> diversifiedArticles = new ArrayList<>();
		int articlesPerSection = Math.max(1, limit / articlesBySection.size());
		
		for (List<Article> sectionArticles : articlesBySection.values()) {
			sectionArticles.stream()
					.map(article -> new ScoredArticle(article, calculateInterestScore(article)))
					.sorted(Comparator.comparingDouble(ScoredArticle::score).reversed())
					.limit(articlesPerSection)
					.map(ScoredArticle::article)
					.forEach(diversifiedArticles::add);
		}
		
		return diversifiedArticles.stream()
				.sorted(Comparator.comparing(Article::getPublicationDate).reversed())
				.limit(limit)
				.collect(Collectors.toList());
	}

	private double calculateInterestScore(Article article) {
		double score = 0.0;
		
		// Recency score (0-50 points): More recent articles are more interesting
		long daysOld = ChronoUnit.DAYS.between(article.getPublicationDate(), LocalDateTime.now());
		double recencyScore = Math.max(0, 50 - (daysOld * 2));
		score += recencyScore;
		
		// Content richness score (0-20 points): Articles with lead and substantial body
		if (article.getLead() != null && !article.getLead().isEmpty()) {
			score += 5;
		}
		if (article.getBody() != null && article.getBody().length() > 200) {
			score += 10;
		}
		if (article.getBody() != null && article.getBody().length() > 500) {
			score += 5;
		}
		
		// Topic diversity score (0-15 points): Articles with multiple sections
		if (article.getSections() != null) {
			score += Math.min(15, article.getSections().length * 5);
		}
		
		// Priority section boost (0-15 points): Breaking news, major events
		if (article.getSections() != null) {
			for (String section : article.getSections()) {
				if ("Actualités".equals(section) || "Breaking".equals(section)) {
					score += 15;
					break;
				}
				if ("Sport".equals(section) || "Culture".equals(section)) {
					score += 10;
					break;
				}
			}
		}
		
		return score;
	}

	private Map<String, List<Article>> groupByPrimarySection(List<Article> articles) {
		Map<String, List<Article>> grouped = new HashMap<>();
		
		for (Article article : articles) {
			String primarySection = getPrimarySection(article);
			grouped.computeIfAbsent(primarySection, k -> new ArrayList<>()).add(article);
		}
		
		return grouped;
	}

	private String getPrimarySection(Article article) {
		if (article.getSections() != null && article.getSections().length > 0) {
			return article.getSections()[0];
		}
		return "Divers";
	}

	public ArticleStatistics analyzeArticleDatabase() {
		List<Article> allArticles = articleRepository.findAll();
		
		Map<String, Long> sectionCounts = new HashMap<>();
		LocalDateTime oldestDate = LocalDateTime.now();
		LocalDateTime newestDate = LocalDateTime.MIN;
		int totalWords = 0;
		
		for (Article article : allArticles) {
			// Count sections
			if (article.getSections() != null) {
				for (String section : article.getSections()) {
					sectionCounts.merge(section, 1L, Long::sum);
				}
			}
			
			// Track date range
			if (article.getPublicationDate().isBefore(oldestDate)) {
				oldestDate = article.getPublicationDate();
			}
			if (article.getPublicationDate().isAfter(newestDate)) {
				newestDate = article.getPublicationDate();
			}
			
			// Count approximate words
			if (article.getBody() != null) {
				totalWords += article.getBody().split("\\s+").length;
			}
		}
		
		return new ArticleStatistics(
				allArticles.size(),
				sectionCounts,
				oldestDate,
				newestDate,
				allArticles.isEmpty() ? 0 : totalWords / allArticles.size()
		);
	}

	private record ScoredArticle(Article article, double score) {}

	public record ArticleStatistics(
			int totalArticles,
			Map<String, Long> sectionDistribution,
			LocalDateTime oldestArticle,
			LocalDateTime newestArticle,
			int averageWordCount
	) {}
}

