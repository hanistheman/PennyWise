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
import org.springframework.test.web.servlet.MockMvc;

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

	@BeforeEach
	void clearExpenses() {
		expenses.deleteAll();
	}

	@Test
	void createsAndListsExpensesNewestFirstAndTrimsText() throws Exception {
		mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"  Coffee ","amount":4.25,"category":" Food & drink ","spentOn":"2026-05-01"}
						"""))
				.andExpect(status().isCreated())
				.andExpect(jsonPath("$.description").value("Coffee"))
				.andExpect(jsonPath("$.category").value("Food & drink"));

		mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"Lunch","amount":12.00,"category":"Food & drink","spentOn":"2026-05-03"}
						"""))
				.andExpect(status().isCreated());

		mockMvc.perform(get("/api/expenses"))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$[0].description").value("Lunch"))
				.andExpect(jsonPath("$[1].description").value("Coffee"));
	}

	@Test
	void rejectsInvalidExpenseAndDoesNotSaveIt() throws Exception {
		mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":" ","amount":0,"category":"Food","spentOn":null}
						"""))
				.andExpect(status().isBadRequest());
		org.junit.jupiter.api.Assertions.assertEquals(0, expenses.count());
	}

	@Test
	void updatesAndDeletesExpenseAndReturnsNotFoundForUnknownIds() throws Exception {
		mockMvc.perform(post("/api/expenses").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"Coffee","amount":4.25,"category":"Food","spentOn":"2026-05-01"}
						"""))
				.andExpect(status().isCreated());
		long expenseId = expenses.findAll().get(0).getId();

		mockMvc.perform(put("/api/expenses/{id}", expenseId).contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":" Groceries ","amount":28.10,"category":"Home","spentOn":"2026-05-02"}
						"""))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.description").value("Groceries"))
				.andExpect(jsonPath("$.amount").value(28.10));

		mockMvc.perform(put("/api/expenses/99999").contentType(MediaType.APPLICATION_JSON)
				.content("""
						{"description":"X","amount":1,"category":"Other","spentOn":"2026-05-02"}
						"""))
				.andExpect(status().isNotFound());
		mockMvc.perform(delete("/api/expenses/{id}", expenseId)).andExpect(status().isNoContent());
		mockMvc.perform(delete("/api/expenses/{id}", expenseId)).andExpect(status().isNotFound());
	}
}
