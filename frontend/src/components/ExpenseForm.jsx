import CurrencyAmountInput from './CurrencyAmountInput.jsx'

function ExpenseForm({
  categories,
  description,
  amount,
  category,
  spentOn,
  saving,
  isEditing,
  onDescriptionChange,
  onAmountChange,
  onCategoryChange,
  onSpentOnChange,
  onSubmit,
  onCancel,
}) {
  return (
    <section className="panel form-panel" id="expense-form">
      <div className="panel-heading form-heading">
        <div>
          <p className="eyebrow">{isEditing ? 'UPDATE YOUR RECORD' : 'KEEP IT UP TO DATE'}</p>
          <h2>{isEditing ? 'Edit expense' : 'Add an expense'}</h2>
        </div>
        <span className="plus-mark">{isEditing ? '✎' : '+'}</span>
      </div>
      <form onSubmit={onSubmit}>
        <label htmlFor="description">What did you spend on?</label>
        <input
          id="description"
          name="description"
          placeholder="e.g. Weekly groceries"
          value={description}
          onChange={onDescriptionChange}
          maxLength={120}
          required
        />

        <div className="form-row">
          <div className="field-group">
            <label htmlFor="amount">Amount</label>
            <CurrencyAmountInput
              id="amount"
              name="amount"
              value={amount}
              onChange={onAmountChange}
            />
          </div>
          <div className="field-group">
            <label htmlFor="spentOn">Date</label>
            <input id="spentOn" name="spentOn" type="date" value={spentOn} onChange={onSpentOnChange} required />
          </div>
        </div>

        <label htmlFor="category">Category</label>
        <select id="category" name="category" value={category} onChange={onCategoryChange}>
          {categories.map((item) => <option key={item}>{item}</option>)}
        </select>

        <div className="form-actions">
          <button className="submit-button" type="submit" disabled={saving}>
            {saving ? 'Saving…' : isEditing ? 'Save changes' : 'Add expense'} <span aria-hidden="true">→</span>
          </button>
          {isEditing && <button className="cancel-edit-button" type="button" onClick={onCancel} disabled={saving}>Cancel</button>}
        </div>
      </form>
      <p className="form-note"><span aria-hidden="true">🔒</span> Your entries are saved to your PennyWise database.</p>
    </section>
  )
}

export default ExpenseForm
