package com.ga.lanceify.proposal.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.jupiter.api.Test;
import org.springframework.data.mongodb.core.MongoTemplate;

import com.ga.lanceify.proposal.client.ContractClient;
import com.ga.lanceify.proposal.client.ProjectClient;
import com.ga.lanceify.proposal.dto.ApplyRequest;
import com.ga.lanceify.proposal.dto.ProjectSummary;
import com.ga.lanceify.proposal.exception.ConflictException;
import com.ga.lanceify.proposal.model.Proposal;
import com.ga.lanceify.proposal.repository.ProposalRepository;

class ProposalLimitConcurrencyTests {

	@Test
	void tenSimultaneousApplicantsOnlyFiveGetIn() throws Exception {
		AtomicInteger stored = new AtomicInteger();
		ProposalRepository repository = mock(ProposalRepository.class);
		when(repository.countByProjectIdAndStatusIn(anyString(), anyList())).thenAnswer(call -> {
			long count = stored.get();
			Thread.sleep(20);
			return count;
		});
		when(repository.insert(any(Proposal.class))).thenAnswer(call -> {
			stored.incrementAndGet();
			return call.getArgument(0);
		});

		ProjectSummary project = new ProjectSummary();
		project.setClientId("client");
		project.setTitle("Site");
		project.setStatus("OPEN");
		ProjectClient projects = mock(ProjectClient.class);
		when(projects.getProject("p1")).thenReturn(project);

		ProposalService service = new ProposalService(repository, projects, mock(ContractClient.class), mock(MongoTemplate.class));
		ApplyRequest request = new ApplyRequest("I can build this for you this month.", BigDecimal.TEN, 5);

		ExecutorService pool = Executors.newFixedThreadPool(10);
		CountDownLatch start = new CountDownLatch(1);
		List<Future<Boolean>> results = new ArrayList<>();
		for (int i = 0; i < 10; i++) {
			String freelancer = "f" + i;
			results.add(pool.submit(() -> {
				start.await();
				try {
					service.apply(freelancer, "p1", request);
					return true;
				} catch (ConflictException e) {
					return false;
				}
			}));
		}
		start.countDown();

		int accepted = 0;
		for (Future<Boolean> result : results) {
			if (result.get()) {
				accepted++;
			}
		}
		pool.shutdown();

		assertEquals(5, accepted);
		assertEquals(5, stored.get());
	}

}
