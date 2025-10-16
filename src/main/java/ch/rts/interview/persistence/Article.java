package ch.rts.interview.persistence;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "articles")
public class Article {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 500)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String lead;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String body;

	@Column(name = "publication_date", nullable = false)
	private LocalDateTime publicationDate;

	@Column(columnDefinition = "text[]")
	private String[] sections;

	@Column(name = "created_at")
	private LocalDateTime createdAt;

	@Column(name = "updated_at")
	private LocalDateTime updatedAt;

	public Article() {
	}

	public Article(String title, String lead, String body, LocalDateTime publicationDate, String[] sections) {
		this.title = title;
		this.lead = lead;
		this.body = body;
		this.publicationDate = publicationDate;
		this.sections = sections;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getLead() {
		return lead;
	}

	public void setLead(String lead) {
		this.lead = lead;
	}

	public String getBody() {
		return body;
	}

	public void setBody(String body) {
		this.body = body;
	}

	public LocalDateTime getPublicationDate() {
		return publicationDate;
	}

	public void setPublicationDate(LocalDateTime publicationDate) {
		this.publicationDate = publicationDate;
	}

	public String[] getSections() {
		return sections;
	}

	public void setSections(String[] sections) {
		this.sections = sections;
	}

	public LocalDateTime getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(LocalDateTime createdAt) {
		this.createdAt = createdAt;
	}

	public LocalDateTime getUpdatedAt() {
		return updatedAt;
	}

	public void setUpdatedAt(LocalDateTime updatedAt) {
		this.updatedAt = updatedAt;
	}

	@PrePersist
	protected void onCreate() {
		createdAt = LocalDateTime.now();
		updatedAt = LocalDateTime.now();
	}

	@PreUpdate
	protected void onUpdate() {
		updatedAt = LocalDateTime.now();
	}
}

