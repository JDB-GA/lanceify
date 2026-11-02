package com.ga.lanceify.seed;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.bson.Document;
import org.bson.types.Decimal128;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.ReplaceOptions;

@Component
public class DemoDataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DemoDataSeeder.class);

	private static final String PASSWORD = "Demo@12345";

	private static final String MARKER_USER = "seed-admin";

	private static final int CATALOG_WAIT_SECONDS = 120;

	private final String mongoUri;

	private final boolean reset;

	private final Date now = Date.from(Instant.now());

	private String encodedPassword;

	private String java;

	private String react;

	private String figma;

	private String webCategory;

	private String designCategory;

	private MongoDatabase identity;

	private MongoDatabase project;

	private MongoDatabase proposal;

	private MongoDatabase contract;

	private MongoDatabase communication;

	public DemoDataSeeder(@Value("${seed.mongo-uri}") String mongoUri, @Value("${seed.reset}") boolean reset) {
		this.mongoUri = mongoUri;
		this.reset = reset;
	}

	@Override
	public void run(String... args) throws Exception {
		try (MongoClient client = MongoClients.create(mongoUri)) {
			identity = client.getDatabase("identity_db");
			project = client.getDatabase("project_db");
			proposal = client.getDatabase("proposal_db");
			contract = client.getDatabase("contract_db");
			communication = client.getDatabase("communication_db");

			if (!reset && identity.getCollection("users").find(Filters.eq("_id", MARKER_USER)).first() != null) {
				log.info("Demo data already exists; set SEED_RESET=true to restore it");
				return;
			}

			loadCatalog(client.getDatabase("catalog_db"));
			encodedPassword = new BCryptPasswordEncoder().encode(PASSWORD);
			seedUsers();
			seedProfiles();
			seedProjectsProposalsContracts();
			seedChats();
			log.info("Demo data ready");
		}
	}

	private void loadCatalog(MongoDatabase catalog) throws InterruptedException {
		for (int waited = 0; waited < CATALOG_WAIT_SECONDS; waited += 2) {
			Document web = catalog.getCollection("categories").find(Filters.eq("name", "Web Development")).first();
			Document design = catalog.getCollection("categories").find(Filters.eq("name", "Design")).first();
			if (web != null && design != null) {
				webCategory = web.getString("_id");
				designCategory = design.getString("_id");
				java = skillId(catalog, "Java");
				react = skillId(catalog, "React");
				figma = skillId(catalog, "Figma");
				return;
			}
			Thread.sleep(2000);
		}
		throw new IllegalStateException("The catalog was not seeded; start catalog-service first");
	}

	private String skillId(MongoDatabase catalog, String name) {
		return catalog.getCollection("skills").find(Filters.eq("name", name)).first().getString("_id");
	}

	private void seedUsers() {
		user("seed-admin", "Demo", "Admin", "admin", "ADMIN", false);

		user("seed-client-omar", "Omar", "Khan", "client.omar", "CLIENT", false);
		user("seed-client-layla", "Layla", "Hassan", "client.layla", "CLIENT", false);
		user("seed-client-new", "Nadia", "Saleh", "client.new", "CLIENT", false);
		user("seed-client-banned", "Bilal", "Banned", "client.banned", "CLIENT", true);

		user("seed-fl-sara", "Sara", "Ali", "freelancer.sara", "FREELANCER", false);
		user("seed-fl-yousif", "Yousif", "Ahmed", "freelancer.yousif", "FREELANCER", false);
		user("seed-fl-noor", "Noor", "Jasim", "freelancer.noor", "FREELANCER", false);
		user("seed-fl-maryam", "Maryam", "Isa", "freelancer.maryam", "FREELANCER", false);
		user("seed-fl-hamad", "Hamad", "Faisal", "freelancer.hamad", "FREELANCER", false);
		user("seed-fl-busy", "Ali", "Capacity", "freelancer.busy", "FREELANCER", false);
		user("seed-fl-spare", "Zainab", "Mirza", "freelancer.spare", "FREELANCER", false);
		user("seed-fl-noprofile", "Tariq", "Noprofile", "freelancer.noprofile", "FREELANCER", false);
		user("seed-fl-draft", "Dana", "Draft", "freelancer.draft", "FREELANCER", false);
		user("seed-fl-pending", "Khalid", "Pending", "freelancer.pending", "FREELANCER", false);
		user("seed-fl-rejected", "Reem", "Rejected", "freelancer.rejected", "FREELANCER", false);
		user("seed-fl-banned", "Badr", "Banned", "freelancer.banned", "FREELANCER", true);
	}

	private void seedProfiles() {
		profile("seed-fl-sara", "Java and Spring developer.", 50, 500, "AVAILABLE", List.of(java), "APPROVED", null);
		profile("seed-fl-yousif", "React front-end developer.", 40, 400, "AVAILABLE", List.of(react), "APPROVED", null);
		profile("seed-fl-noor", "Full-stack developer.", 80, 800, "BUSY", List.of(java, react), "APPROVED", null);
		profile("seed-fl-maryam", "Product designer.", 30, 300, "AVAILABLE", List.of(figma), "APPROVED", null);
		profile("seed-fl-hamad", "Web developer.", 60, 600, "AVAILABLE", List.of(java), "APPROVED", null);
		profile("seed-fl-busy", "Has reached the contract limit.", 70, 700, "BUSY", List.of(java), "APPROVED", null);
		profile("seed-fl-spare", "Approved with no proposals yet.", 45, 450, "AVAILABLE", List.of(react), "APPROVED", null);
		profile("seed-fl-draft", "Still writing this profile.", 20, 200, "AVAILABLE", List.of(), "DRAFT", null);
		profile("seed-fl-pending", "Waiting for admin review.", 35, 350, "AVAILABLE", List.of(java), "PENDING", null);
		profile("seed-fl-rejected", "Short bio.", 10, 100, "AVAILABLE", List.of(react), "REJECTED",
				"Please add more detail about your experience");
		profile("seed-fl-banned", "Banned account.", 25, 250, "AVAILABLE", List.of(java), "APPROVED", null);
	}

	private void seedProjectsProposalsContracts() {
		String omar = "seed-client-omar";
		String layla = "seed-client-layla";

		project("seed-prj-open-empty", omar, null, "Company website", webCategory, List.of(java), 1000, "OPEN");

		project("seed-prj-open-some", omar, null, "Booking system", webCategory, List.of(java, react), 2000, "OPEN");
		bid("seed-prp-some-1", "seed-prj-open-some", "Booking system", omar, "seed-fl-sara", 1800, "PENDING");
		bid("seed-prp-some-2", "seed-prj-open-some", "Booking system", omar, "seed-fl-yousif", 1700, "PENDING");
		bid("seed-prp-some-3", "seed-prj-open-some", "Booking system", omar, "seed-fl-noor", 1900, "WITHDRAWN");
		bid("seed-prp-some-4", "seed-prj-open-some", "Booking system", omar, "seed-fl-hamad", 2500, "REJECTED");

		project("seed-prj-open-almost", layla, null, "Online store", webCategory, List.of(react), 1500, "OPEN");
		for (String[] item : new String[][] { { "1", "seed-fl-sara" }, { "2", "seed-fl-yousif" }, { "3", "seed-fl-noor" },
				{ "4", "seed-fl-hamad" } }) {
			bid("seed-prp-almost-" + item[0], "seed-prj-open-almost", "Online store", layla, item[1], 1400, "PENDING");
		}

		project("seed-prj-open-full", layla, null, "Mobile landing page", webCategory, List.of(react), 800, "OPEN");
		for (String[] item : new String[][] { { "1", "seed-fl-sara" }, { "2", "seed-fl-yousif" }, { "3", "seed-fl-noor" },
				{ "4", "seed-fl-hamad" }, { "5", "seed-fl-maryam" } }) {
			bid("seed-prp-full-" + item[0], "seed-prj-open-full", "Mobile landing page", layla, item[1], 750, "PENDING");
		}

		hired("seed-prj-active", omar, "seed-fl-sara", "Inventory dashboard", webCategory, List.of(java), 3000, "ACTIVE");
		bid("seed-prp-active-lost", "seed-prj-active", "Inventory dashboard", omar, "seed-fl-yousif", 3100, "REJECTED");

		hired("seed-prj-completed", omar, "seed-fl-yousif", "Portfolio site", webCategory, List.of(react), 600, "COMPLETED");
		hired("seed-prj-cancelled-contract", layla, "seed-fl-noor", "Logo refresh", designCategory, List.of(figma), 200, "CANCELLED");

		project("seed-prj-cancelled", layla, null, "Cancelled idea", designCategory, List.of(figma), 300, "CANCELLED");

		for (int i = 1; i <= 3; i++) {
			hired("seed-prj-busy-" + i, layla, "seed-fl-busy", "Capacity project " + i, webCategory, List.of(java), 900, "ACTIVE");
		}
		project("seed-prj-capacity-test", omar, null, "Capacity test project", webCategory, List.of(java), 950, "OPEN");
		bid("seed-prp-capacity-test", "seed-prj-capacity-test", "Capacity test project", omar, "seed-fl-busy", 900, "PENDING");
	}

	private void seedChats() {
		chat("seed-chat-unread", "seed-client-omar", "seed-fl-sara", "seed-prj-active", List.of(
				message("seed-msg-1", "seed-chat-unread", "seed-client-omar", "seed-fl-sara", "Hello Sara, how is the dashboard going?", true),
				message("seed-msg-2", "seed-chat-unread", "seed-fl-sara", "seed-client-omar", "Good, I will share a first version tomorrow.", true),
				message("seed-msg-3", "seed-chat-unread", "seed-client-omar", "seed-fl-sara", "Great, thank you.", false)));
		chat("seed-chat-read", "seed-client-omar", "seed-fl-yousif", "seed-prj-completed", List.of(
				message("seed-msg-4", "seed-chat-read", "seed-fl-yousif", "seed-client-omar", "The portfolio site is live.", true),
				message("seed-msg-5", "seed-chat-read", "seed-client-omar", "seed-fl-yousif", "Thanks, it looks great.", true)));
		chat("seed-chat-empty", "seed-client-layla", "seed-fl-maryam", null, List.of());
	}

	private void user(String id, String first, String last, String username, String role, boolean banned) {
		Document doc = new Document("_id", id)
				.append("firstName", first)
				.append("lastName", last)
				.append("username", username)
				.append("email", username + "@lanceify.demo")
				.append("password", encodedPassword)
				.append("role", role)
				.append("banned", banned)
				.append("createdAt", now)
				.append("updatedAt", now);
		put(identity, "users", doc);
	}

	private void profile(String userId, String bio, int min, int max, String availability, List<String> skills,
			String status, String reason) {
		Document doc = new Document("_id", userId + "-profile")
				.append("userId", userId)
				.append("bio", bio)
				.append("minPrice", money(min))
				.append("maxPrice", money(max))
				.append("availability", availability)
				.append("skillIds", skills)
				.append("approvalStatus", status)
				.append("rejectionReason", reason)
				.append("version", 0L);
		if (!"DRAFT".equals(status)) {
			doc.append("submittedAt", now);
		}
		if ("APPROVED".equals(status) || "REJECTED".equals(status)) {
			doc.append("reviewedAt", now);
		}
		put(identity, "freelancer_profiles", doc);
	}

	private void project(String id, String clientId, String freelancerId, String title, String category,
			List<String> skills, int budget, String status) {
		Document doc = new Document("_id", id)
				.append("clientId", clientId)
				.append("assignedFreelancerId", freelancerId)
				.append("title", title)
				.append("description", title + " for the Lanceify demo.")
				.append("budget", money(budget))
				.append("deadline", Date.from(Instant.now().plus(60, ChronoUnit.DAYS)))
				.append("categoryId", category)
				.append("status", status)
				.append("requiredSkillIds", skills)
				.append("createdAt", now);
		put(project, "projects", doc);
	}

	private void bid(String id, String projectId, String title, String clientId, String freelancerId, int amount, String status) {
		Document doc = new Document("_id", id)
				.append("projectId", projectId)
				.append("clientId", clientId)
				.append("projectTitle", title)
				.append("freelancerId", freelancerId)
				.append("coverLetter", "I have delivered similar work and can start right away.")
				.append("bidAmount", money(amount))
				.append("deliveryDays", 14)
				.append("status", status)
				.append("createdAt", now)
				.append("updatedAt", now)
				.append("version", 0L);
		if (!"PENDING".equals(status)) {
			doc.append("decidedAt", now);
		}
		put(proposal, "proposals", doc);
	}

	private void hired(String projectId, String clientId, String freelancerId, String title, String category,
			List<String> skills, int amount, String contractStatus) {
		String projectStatus = switch (contractStatus) {
			case "ACTIVE" -> "IN_PROGRESS";
			case "COMPLETED" -> "COMPLETED";
			default -> "CANCELLED";
		};
		project(projectId, clientId, freelancerId, title, category, skills, amount, projectStatus);
		String proposalId = projectId.replace("seed-prj", "seed-prp");
		bid(proposalId, projectId, title, clientId, freelancerId, amount, "ACCEPTED");
		Document doc = new Document("_id", projectId.replace("seed-prj", "seed-ctr"))
				.append("proposalId", proposalId)
				.append("projectId", projectId)
				.append("projectTitle", title)
				.append("clientId", clientId)
				.append("freelancerId", freelancerId)
				.append("amount", money(amount))
				.append("deliveryDays", 14)
				.append("status", contractStatus)
				.append("createdAt", now);
		if (!"ACTIVE".equals(contractStatus)) {
			doc.append("closedAt", now);
		}
		put(contract, "contracts", doc);
	}

	private Document message(String id, String conversationId, String sender, String recipient, String content, boolean read) {
		return new Document("_id", id)
				.append("conversationId", conversationId)
				.append("clientMessageId", id)
				.append("senderId", sender)
				.append("recipientId", recipient)
				.append("content", content)
				.append("read", read)
				.append("createdAt", now);
	}

	private void chat(String id, String first, String second, String projectId, List<Document> messages) {
		List<Document> stored = new ArrayList<>(messages);
		Document conversation = new Document("_id", id)
				.append("participantIds", List.of(first, second))
				.append("participantKey", participantKey(first, second, projectId))
				.append("projectId", projectId)
				.append("lastMessage", stored.isEmpty() ? null : stored.get(stored.size() - 1).getString("content"))
				.append("lastMessageAt", now)
				.append("createdAt", now);
		put(communication, "conversations", conversation);
		stored.forEach(message -> put(communication, "messages", message));
	}

	private String participantKey(String first, String second, String projectId) {
		String pair = first.compareTo(second) < 0 ? first + "|" + second : second + "|" + first;
		return pair + "#" + (projectId == null ? "" : projectId);
	}

	private Decimal128 money(int amount) {
		return new Decimal128(BigDecimal.valueOf(amount));
	}

	private void put(MongoDatabase database, String collection, Document document) {
		database.getCollection(collection).replaceOne(Filters.eq("_id", document.get("_id")), document,
				new ReplaceOptions().upsert(true));
	}

}
