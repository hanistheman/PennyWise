import CurrencyAmountInput from './CurrencyAmountInput.jsx'

function BudgetForm({ amount, onAmountChange, onSubmit, hasBudget, saving }) {
  return (
    <form className="budget-form" onSubmit={onSubmit}>
      <label htmlFor="monthlyBudget">{hasBudget ? 'Update monthly limit' : 'Monthly limit'}</label>
      <div className="budget-form-row">
        <CurrencyAmountInput
          id="monthlyBudget"
          name="monthlyBudget"
          min="0.01"
          step="0.01"
          placeholder="e.g. 2500"
          value={amount}
          onChange={onAmountChange}
        />
        <button className="budget-save-button" type="submit" disabled={saving}>
          {saving ? 'Saving…' : hasBudget ? 'Update' : 'Set budget'}
        </button>
      </div>
    </form>
  )
}

export default BudgetForm
