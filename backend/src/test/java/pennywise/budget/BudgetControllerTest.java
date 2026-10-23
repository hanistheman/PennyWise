package pennywise.budget;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:budgets;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false",
		"spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class BudgetControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired MonthlyBudgetRepository monthlyBudgets;
	@Autowired CategoryMonthlyBudgetRepository categoryBudgets;

	@BeforeEach
	void clearBudgets() {
		categoryBudgets.deleteAll();
		monthlyBudgets.deleteAll();
	}

	@Test
	void monthlyBudgetCanBeReadCreatedAndUpdated() throws Exception {
		mockMvc.perform(get("/api/budgets/2026-05")).andExpect(status().isNoContent());

		mockMvc.perform(put("/api/budgets/2026-05").contentType(MediaType.APPLICATION_JSON)
				.content("{\"amount\":1200.00}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.budgetMonth").value("2026-05"))
				.andExpect(jsonPath("$.amount").value(1200.00));

		mockMvc.perform(put("/api/budgets/2026-05").contentType(MediaType.APPLICATION_JSON)
				.content("{\"amount\":1350.50}"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(1350.50));
		org.junit.jupiter.api.Assertions.assertEquals(1, monthlyBudgets.count());
		mockMvc.perform(get("/api/budgets/2026-05")).andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(1350.50));
	}

	@Test
	void monthlyBudgetRejectsMissingOrNonPositiveAmount() throws Exception {
		mockMvc.perform(put("/api/budgets/2026-05").contentType(MediaType.APPLICATION_JSON)
				.content("{\"amount\":0}"))
				.andExpect(status().isBadRequest());
		mockMvc.perform(put("/api/budgets/2026-05").contentType(MediaType.APPLICATION_JSON)
				.content("{}"))
				.andExpect(status().isBadRequest());
	}

	@Test
	void categoryBudgetsAreListedInOrderAndSameCategoryIsUpdated() throws Exception {
		putCategory("Transport", 300.00).andExpect(status().isOk());
		putCategory("Food & drink", 500.00).andExpect(status().isOk());
		putCategory("Transport", 350.00).andExpect(status().isOk())
				.andExpect(jsonPath("$.amount").value(350.00));

		org.junit.jupiter.api.Assertions.assertEquals(2, categoryBudgets.count());
		mockMvc.perform(get("/api/category-budgets/2026-05"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].category").value("Food & drink"))
				.andExpect(jsonPath("$[1].category").value("Transport"));
	}

	@Test
	void categoryBudgetRejectsUnsupportedCategoryAndInvalidAmount() throws Exception {
		putCategory("Utilities", 100.00).andExpect(status().isBadRequest());
		putCategory("Food & drink", 0).andExpect(status().isBadRequest());
		org.junit.jupiter.api.Assertions.assertEquals(0, categoryBudgets.count());
	}

	private org.springframework.test.web.servlet.ResultActions putCategory(String category, double amount)
			throws Exception {
		return mockMvc.perform(put("/api/category-budgets/2026-05").contentType(MediaType.APPLICATION_JSON)
				.content("{\"category\":\"" + category + "\",\"amount\":" + amount + "}"));
	}
}
