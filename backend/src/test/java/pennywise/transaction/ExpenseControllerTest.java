package pennywise.transaction;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;

import java.util.UUID;

@SpringBootTest(properties = {
		"spring.datasource.url=jdbc:h2:mem:expenses;DB_CLOSE_DELAY=-1;MODE=PostgreSQL",
		"spring.datasource.driver-class-name=org.h2.Driver",
		"spring.datasource.username=sa",
		"spring.datasource.password=",
		"spring.jpa.hibernate.ddl-auto=create-drop",
		"spring.jpa.show-sql=false",
		"spring.sql.init.mode=never"
})
@AutoConfigureMockMvc
class ExpenseControllerTest {

	@Autowired MockMvc mockMvc;
	@Autowired ExpenseRepository expenses;
	private MockHttpSession authenticatedSession;

	@BeforeEach
	void clearExpensesAndAuthenticate() throws Exception {
		expenses.deleteAll();
		MvcResult result = mockMvc.perform(post("/api/auth/register").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"email":"%s@example.com","password":"test-password"}
						""".formatted(UUID.randomUUID())))
				.andExpect(status().isCreated())
				.andReturn();
		authenticatedSession = (MockHttpSession) result.getRequest().getSession(false);
	}

	@Test
	void createsAndListsExpensesNewestFirstAndTrimsText() throws Exception {
		perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"  Coffee ","amount":4.25,"category":" Food & drink ","spentOn":"2026-05-01"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description").value("Coffee"))
				.andExpect(jsonPath("$.category").value("Food & drink"));

		perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"Lunch","amount":12.00,"category":"Food & drink","spentOn":"2026-05-03"}
						"""))
				.andExpect(status().isCreated());

		perform(get("/api/expenses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].description").value("Lunch"))
				.andExpect(jsonPath("$[1].description").value("Coffee"));
	}

	@Test
	void monthlyTotalsGroupExpensesByMonthInChronologicalOrder() throws Exception {
		createExpense("Coffee", "4.25", "2026-05-01");
		createExpense("Lunch", "12.00", "2026-05-03");
		createExpense("Groceries", "28.10", "2026-04-30");

		perform(get("/api/expenses/monthly-totals"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].month").value("2026-04"))
				.andExpect(jsonPath("$[0].amount").value(28.10))
				.andExpect(jsonPath("$[1].month").value("2026-05"))
				.andExpect(jsonPath("$[1].amount").value(16.25));
	}

	@Test
	void rejectsInvalidExpenseAndDoesNotSaveIt() throws Exception {
		perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":" ","amount":0,"category":"Food","spentOn":null}
						"""))
				.andExpect(status().isBadRequest());
		org.junit.jupiter.api.Assertions.assertEquals(0, expenses.count());
	}

	@Test
	void updatesAndDeletesExpenseAndReturnsNotFoundForUnknownIds() throws Exception {
		perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"Coffee","amount":4.25,"category":"Food","spentOn":"2026-05-01"}
						"""))
				.andExpect(status().isCreated());
		long expenseId = expenses.findAll().get(0).getId();

		perform(put("/api/expenses/{id}", expenseId).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":" Groceries ","amount":28.10,"category":"Home","spentOn":"2026-05-02"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description").value("Groceries"))
				.andExpect(jsonPath("$.amount").value(28.10));

		perform(put("/api/expenses/99999").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"X","amount":1,"category":"Other","spentOn":"2026-05-02"}
						"""))
				.andExpect(status().isNotFound());
		perform(delete("/api/expenses/{id}", expenseId)).andExpect(status().isNoContent());
		perform(delete("/api/expenses/{id}", expenseId)).andExpect(status().isNotFound());
	}

	private void createExpense(String description, String amount, String spentOn) throws Exception {
		perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"%s","amount":%s,"category":"Other","spentOn":"%s"}
						""".formatted(description, amount, spentOn)))
				.andExpect(status().isCreated());
	}

	private ResultActions perform(MockHttpServletRequestBuilder request) throws Exception {
		return mockMvc.perform(request.session(authenticatedSession));
	}
}
