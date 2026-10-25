import { useMemo, useState } from 'react'

const currency = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
})

const expenseDate = new Intl.DateTimeFormat('en-US', {
  month: 'short',
  day: 'numeric',
  year: 'numeric',
})
const expenseMonth = new Intl.DateTimeFormat('en-US', { month: 'long', year: 'numeric' })

function formatMonth(monthKey) {
  const [year, month] = monthKey.split('-').map(Number)
  return expenseMonth.format(new Date(year, month - 1, 1, 12))
}

function ExpenseList({
  expenses,
  categories,
  loading,
  error,
  saving,
  deletingExpenseId,
  onEdit,
  onDelete,
}) {
  const [search, setSearch] = useState('')
  const [monthFilter, setMonthFilter] = useState('')
  const [categoryFilter, setCategoryFilter] = useState('')
  const [dateFrom, setDateFrom] = useState('')
  const [dateTo, setDateTo] = useState('')

  const availableMonths = useMemo(() => [...new Set(expenses.map((expense) => expense.spentOn.slice(0, 7)))].sort().reverse(), [expenses])

  const filteredExpenses = useMemo(() => {
    const query = search.trim().toLocaleLowerCase()
    return expenses.filter((expense) => {
      const matchesQuery = !query || `${expense.description} ${expense.category}`.toLocaleLowerCase().includes(query)
      const matchesMonth = !monthFilter || expense.spentOn.startsWith(monthFilter)
      const matchesCategory = !categoryFilter || expense.category === categoryFilter
      const matchesStartDate = !dateFrom || expense.spentOn >= dateFrom
      const matchesEndDate = !dateTo || expense.spentOn <= dateTo
      return matchesQuery && matchesMonth && matchesCategory && matchesStartDate && matchesEndDate
    })
  }, [expenses, search, monthFilter, categoryFilter, dateFrom, dateTo])

  const hasFilters = Boolean(search || monthFilter || categoryFilter || dateFrom || dateTo)

  function clearFilters() {
    setSearch('')
    setMonthFilter('')
    setCategoryFilter('')
    setDateFrom('')
    setDateTo('')
  }

  return (
    <section className="panel transactions-panel" aria-labelledby="recent-expenses-title">
      <div className="panel-heading">
        <div>
          <h2 id="recent-expenses-title">Recent expenses</h2>
          <p>{hasFilters ? `Showing ${filteredExpenses.length} of ${expenses.length} expenses` : 'Your latest spending activity'}</p>
        </div>
        <span className="count-pill" aria-label={`${filteredExpenses.length} expenses shown`}>{filteredExpenses.length}</span>
      </div>

      <div className="expense-filters" role="search" aria-label="Filter expenses">
        <label className="expense-search-field">
          <span className="visually-hidden">Search expenses</span>
          <span className="expense-search-icon" aria-hidden="true">⌕</span>
          <input
            type="search"
            placeholder="Search description or category"
            value={search}
            onChange={(event) => setSearch(event.target.value)}
          />
        </label>
        <label className="expense-filter-field">
          <span className="visually-hidden">Filter by category</span>
          <select value={categoryFilter} onChange={(event) => setCategoryFilter(event.target.value)}>
            <option value="">All categories</option>
            {categories.map((name) => <option key={name} value={name}>{name}</option>)}
          </select>
        </label>
        <label className="expense-filter-field expense-month-filter">
          <span className="visually-hidden">Filter by month</span>
          <select value={monthFilter} onChange={(event) => setMonthFilter(event.target.value)}>
            <option value="">All months</option>
            {availableMonths.map((month) => <option key={month} value={month}>{formatMonth(month)}</option>)}
          </select>
        </label>
        <label className="expense-filter-field expense-date-filter">
          <span className="visually-hidden">From date</span>
          <input type="date" aria-label="From date" value={dateFrom} max={dateTo || undefined} onChange={(event) => setDateFrom(event.target.value)} />
        </label>
        <label className="expense-filter-field expense-date-filter">
          <span className="visually-hidden">To date</span>
          <input type="date" aria-label="To date" value={dateTo} min={dateFrom || undefined} onChange={(event) => setDateTo(event.target.value)} />
        </label>
        {hasFilters && <button className="clear-expense-filters" type="button" onClick={clearFilters}>Clear</button>}
      </div>

      {error && <p className="error-message" role="alert">{error}</p>}

      {loading ? (
        <div className="empty-state"><span className="loading-dot" /> Loading expenses…</div>
      ) : expenses.length === 0 ? (
        <div className="empty-state">
          <span className="empty-icon">✳</span>
          <strong>No expenses yet</strong>
          <span>Add your first expense to start seeing your spending here.</span>
        </div>
      ) : filteredExpenses.length === 0 ? (
        <div className="empty-state filter-empty-state">
          <strong>No matching expenses</strong>
          <span>Try changing your search or filters.</span>
          <button className="clear-expense-filters" type="button" onClick={clearFilters}>Clear filters</button>
        </div>
      ) : (
        <ul className="expense-list">
          {filteredExpenses.map((expense) => (
            <li className="expense-row" key={expense.id}>
              <span className="category-icon" aria-hidden="true">{expense.category.slice(0, 1)}</span>
              <div className="expense-main">
                <strong>{expense.description}</strong>
                <span>{expense.category} <span className="separator">·</span> {expenseDate.format(new Date(`${expense.spentOn}T12:00:00`))}</span>
              </div>
              <strong className="expense-amount">−{currency.format(Number(expense.amount))}</strong>
              <div className="expense-actions">
                <button
                  className="edit-expense-button"
                  type="button"
                  aria-label={`Edit ${expense.description}`}
                  disabled={saving || deletingExpenseId !== null}
                  onClick={() => onEdit(expense)}
                >
                  Edit
                </button>
                <button
                  className="delete-expense-button"
                  type="button"
                  aria-label={`Delete ${expense.description}`}
                  title="Delete expense"
                  disabled={saving || deletingExpenseId !== null}
                  onClick={() => onDelete(expense)}
                >
                  {deletingExpenseId === expense.id ? 'Deleting…' : 'Delete'}
                </button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default ExpenseList
