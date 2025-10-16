package ch.rts.interview.gateway;

import ch.rts.interview.persistence.Article;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/gateway")
public class ArticleGatewayController {

	private final ArticleAnalysisService analysisService;
	private final ArticleRecommendationService recommendationService;

	public ArticleGatewayController(ArticleAnalysisService analysisService,
	                                ArticleRecommendationService recommendationService) {
		this.analysisService = analysisService;
		this.recommendationService = recommendationService;
	}

	@GetMapping("/interesting")
	public ResponseEntity<List<Article>> getInterestingArticles(
			@RequestParam(defaultValue = "10") int limit) {
		List<Article> articles = analysisService.getInterestingArticles(limit);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/interesting/diversified")
	public ResponseEntity<List<Article>> getInterestingArticlesDiversified(
			@RequestParam(defaultValue = "10") int limit) {
		List<Article> articles = analysisService.getInterestingArticlesDiversified(limit);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/top-stories")
	public ResponseEntity<List<Article>> getTopStories(
			@RequestParam(defaultValue = "5") int count) {
		List<Article> articles = recommendationService.getTopStories(count);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/daily-digest")
	public ResponseEntity<List<Article>> getDailyDigest() {
		List<Article> articles = recommendationService.getDailyDigest();
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/trending/{section}")
	public ResponseEntity<List<Article>> getTrendingBySection(
			@PathVariable String section,
			@RequestParam(defaultValue = "5") int count) {
		List<Article> articles = recommendationService.getTrendingBySection(section, count);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/highlights")
	public ResponseEntity<Map<String, List<Article>>> getHighlightsByCategory(
			@RequestParam(defaultValue = "3") int articlesPerCategory) {
		Map<String, List<Article>> highlights = recommendationService.getHighlightsByCategory(articlesPerCategory);
		return ResponseEntity.ok(highlights);
	}

	@GetMapping("/breaking-news")
	public ResponseEntity<List<Article>> getRecentBreakingNews(
			@RequestParam(defaultValue = "24") int hours) {
		List<Article> articles = recommendationService.getRecentBreakingNews(hours);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/similar/{articleId}")
	public ResponseEntity<List<Article>> getSimilarArticles(
			@PathVariable Long articleId,
			@RequestParam(defaultValue = "5") int count) {
		List<Article> articles = recommendationService.getSimilarArticles(articleId, count);
		return ResponseEntity.ok(articles);
	}

	@GetMapping("/statistics")
	public ResponseEntity<ArticleAnalysisService.ArticleStatistics> getStatistics() {
		ArticleAnalysisService.ArticleStatistics stats = analysisService.analyzeArticleDatabase();
		return ResponseEntity.ok(stats);
	}
}

