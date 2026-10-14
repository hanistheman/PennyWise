import { useEffect, useMemo, useState } from 'react'
import './App.css'

const categories = ['Food & drink', 'Transport', 'Shopping', 'Home', 'Health', 'Other']

const currency = new Intl.NumberFormat('en-US', {
  style: 'currency',
  currency: 'USD',
})

function localDateToday() {
  const now = new Date()
  return new Date(now.getTime() - now.getTimezoneOffset() * 60000)
    .toISOString()
    .slice(0, 10)
}

function App() {
  const [expenses, setExpenses] = useState([])
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState(categories[0])
  const [spentOn, setSpentOn] = useState(localDateToday)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [error, setError] = useState('')

  useEffect(() => {
    let cancelled = false

    fetch('/api/expenses')
      .then((response) => {
        if (!response.ok) throw new Error('Could not load expenses. Is the backend running?')
        return response.json()
      })
      .then((data) => {
        if (!cancelled) setExpenses(data)
      })
      .catch((requestError) => {
        if (!cancelled) setError(requestError.message)
      })
      .finally(() => {
        if (!cancelled) setLoading(false)
      })

    return () => {
      cancelled = true
    }
  }, [])

  const totalSpent = useMemo(
    () => expenses.reduce((total, expense) => total + Number(expense.amount), 0),
    [expenses],
  )

  const monthlyTotal = useMemo(() => {
    const currentMonth = localDateToday().slice(0, 7)
    return expenses
      .filter((expense) => expense.spentOn.startsWith(currentMonth))
      .reduce((total, expense) => total + Number(expense.amount), 0)
  }, [expenses])

  async function handleSubmit(event) {
    event.preventDefault()
    setError('')
    setSaving(true)

    try {
      const response = await fetch('/api/expenses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({
          description,
          amount: Number(amount),
          category,
          spentOn,
        }),
      })

      if (!response.ok) throw new Error('Could not save this expense. Check the details and try again.')

      const savedExpense = await response.json()
      setExpenses((current) => [savedExpense, ...current].sort((a, b) =>
        b.spentOn.localeCompare(a.spentOn) || b.id - a.id,
      ))
      setDescription('')
      setAmount('')
      setCategory(categories[0])
      setSpentOn(localDateToday())
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  return (
    <main className="app-shell">
      <header className="topbar">
        <a className="brand" href="/" aria-label="PennyWise home">
          <span className="brand-mark">P</span>
          <span>PennyWise</span>
        </a>
        <div className="profile-chip" aria-label="Personal account">
          <span className="profile-avatar">H</span>
          <span className="profile-label">My finances</span>
        </div>
      </header>

      <section className="page-heading">
        <div>
          <p className="eyebrow">YOUR MONEY, IN FOCUS</p>
          <h1>Expenses</h1>
          <p className="subtitle">A clear view of where your money goes.</p>
        </div>
        <span className="date-badge">{new Intl.DateTimeFormat('en-US', { dateStyle: 'full' }).format(new Date())}</span>
      </section>

      <section className="summary-grid" aria-label="Spending summary">
        <article className="summary-card summary-card-primary">
          <div className="summary-topline"><span className="summary-icon">↗</span><span>ALL RECORDED</span></div>
          <p className="summary-value">{currency.format(totalSpent)}</p>
          <p className="summary-caption">Total expenses tracked</p>
        </article>
        <article className="summary-card">
          <div className="summary-topline"><span className="summary-icon soft-icon">◷</span><span>THIS MONTH</span></div>
          <p className="summary-value">{currency.format(monthlyTotal)}</p>
          <p className="summary-caption">Spent so far this month</p>
        </article>
        <article className="summary-card">
          <div className="summary-topline"><span className="summary-icon soft-icon">≡</span><span>TRANSACTIONS</span></div>
          <p className="summary-value">{expenses.length}</p>
          <p className="summary-caption">Expenses recorded</p>
        </article>
      </section>

      <section className="workspace-grid">
        <section className="panel transactions-panel">
          <div className="panel-heading">
            <div>
              <h2>Recent expenses</h2>
              <p>Your latest spending activity</p>
            </div>
            <span className="count-pill">{expenses.length}</span>
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
          ) : (
            <ul className="expense-list">
              {expenses.map((expense) => (
                <li className="expense-row" key={expense.id}>
                  <span className="category-icon" aria-hidden="true">{expense.category.slice(0, 1)}</span>
                  <div className="expense-main">
                    <strong>{expense.description}</strong>
                    <span>{expense.category} <span className="separator">·</span> {new Intl.DateTimeFormat('en-US', { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(`${expense.spentOn}T12:00:00`))}</span>
                  </div>
                  <strong className="expense-amount">−{currency.format(Number(expense.amount))}</strong>
                </li>
              ))}
            </ul>
          )}
        </section>

        <section className="panel form-panel">
          <div className="panel-heading form-heading">
            <div>
              <p className="eyebrow">KEEP IT UP TO DATE</p>
              <h2>Add an expense</h2>
            </div>
            <span className="plus-mark">+</span>
          </div>
          <form onSubmit={handleSubmit}>
            <label htmlFor="description">What did you spend on?</label>
            <input
              id="description"
              name="description"
              placeholder="e.g. Weekly groceries"
              value={description}
              onChange={(event) => setDescription(event.target.value)}
              maxLength={120}
              required
            />

            <div className="form-row">
              <div className="field-group">
                <label htmlFor="amount">Amount</label>
                <div className="amount-input"><span>$</span><input id="amount" name="amount" type="number" min="0.01" step="0.01" placeholder="0.00" value={amount} onChange={(event) => setAmount(event.target.value)} required /></div>
              </div>
              <div className="field-group">
                <label htmlFor="spentOn">Date</label>
                <input id="spentOn" name="spentOn" type="date" value={spentOn} onChange={(event) => setSpentOn(event.target.value)} required />
              </div>
            </div>

            <label htmlFor="category">Category</label>
            <select id="category" name="category" value={category} onChange={(event) => setCategory(event.target.value)}>
              {categories.map((item) => <option key={item}>{item}</option>)}
            </select>

            <button className="submit-button" type="submit" disabled={saving}>
              {saving ? 'Saving…' : 'Add expense'} <span aria-hidden="true">→</span>
            </button>
          </form>
          <p className="form-note"><span>🔒</span> Your entries are saved to your PennyWise database.</p>
        </section>
      </section>
      <footer className="footer">Small steps today. More confidence tomorrow.</footer>
    </main>
  )
}

export default App
