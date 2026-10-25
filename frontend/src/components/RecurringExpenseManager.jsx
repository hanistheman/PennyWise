import { useEffect, useState } from 'react'

const currency = (amount, currencyCode) => new Intl.NumberFormat(undefined, {
  style: 'currency',
  currency: currencyCode,
}).format(Number(amount))

function localDateToday() {
  const now = new Date()
  return new Date(now.getTime() - now.getTimezoneOffset() * 60000).toISOString().slice(0, 10)
}

const frequencyLabels = {
  WEEKLY: 'Weekly',
  MONTHLY: 'Monthly',
  YEARLY: 'Yearly',
}

function RecurringExpenseManager({ categories, currencyCode = 'USD' }) {
  const [recurringExpenses, setRecurringExpenses] = useState([])
  const [description, setDescription] = useState('')
  const [amount, setAmount] = useState('')
  const [category, setCategory] = useState(categories[0] || '')
  const [frequency, setFrequency] = useState('MONTHLY')
  const [startDate, setStartDate] = useState(localDateToday)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)
  const [busyId, setBusyId] = useState(null)
  const [error, setError] = useState('')

  useEffect(() => {
    if (categories.length && !categories.includes(category)) setCategory(categories[0])
  }, [categories, category])

  useEffect(() => {
    let cancelled = false
    fetch('/api/recurring-expenses')
      .then((response) => {
        if (!response.ok) throw new Error('Could not load recurring expenses.')
        return response.json()
      })
      .then((data) => { if (!cancelled) setRecurringExpenses(data) })
      .catch((requestError) => { if (!cancelled) setError(requestError.message) })
      .finally(() => { if (!cancelled) setLoading(false) })
    return () => { cancelled = true }
  }, [])

  async function createRecurringExpense(event) {
    event.preventDefault()
    setSaving(true)
    setError('')
    try {
      const response = await fetch('/api/recurring-expenses', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ description, amount: Number(amount), category, frequency, startDate }),
      })
      if (!response.ok) throw new Error('Could not save this recurring expense. Check the details and try again.')
      const saved = await response.json()
      setRecurringExpenses((current) => [...current, saved].sort((a, b) => Number(b.active) - Number(a.active) || a.nextDate.localeCompare(b.nextDate)))
      setDescription('')
      setAmount('')
      setStartDate(localDateToday())
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setSaving(false)
    }
  }

  async function toggleActive(item) {
    setBusyId(item.id)
    setError('')
    try {
      const response = await fetch(`/api/recurring-expenses/${item.id}/status`, {
        method: 'PUT',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ active: !item.active }),
      })
      if (!response.ok) throw new Error('Could not update this recurring expense.')
      const updated = await response.json()
      setRecurringExpenses((current) => current.map((entry) => entry.id === updated.id ? updated : entry))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setBusyId(null)
    }
  }

  async function remove(item) {
    setBusyId(item.id)
    setError('')
    try {
      const response = await fetch(`/api/recurring-expenses/${item.id}`, { method: 'DELETE' })
      if (!response.ok) throw new Error('Could not delete this recurring expense.')
      setRecurringExpenses((current) => current.filter((entry) => entry.id !== item.id))
    } catch (requestError) {
      setError(requestError.message)
    } finally {
      setBusyId(null)
    }
  }

  return (
    <section className="panel recurring-panel" aria-labelledby="recurring-expenses-title">
      <div className="panel-heading">
        <div>
          <h2 id="recurring-expenses-title">Recurring expenses</h2>
          <p>Automatically add regular bills and purchases on their due dates.</p>
        </div>
      </div>

      <form className="recurring-form" onSubmit={createRecurringExpense}>
        <div className="recurring-field recurring-description-field">
          <label htmlFor="recurring-description">Description</label>
          <input id="recurring-description" value={description} onChange={(event) => setDescription(event.target.value)} maxLength={120} placeholder="e.g. Rent" required />
        </div>
        <div className="recurring-field">
          <label htmlFor="recurring-amount">Amount</label>
          <div className="recurring-amount-input">
            <span aria-hidden="true">{new Intl.NumberFormat(undefined, { style: 'currency', currency: currencyCode }).formatToParts(0).find((part) => part.type === 'currency')?.value || currencyCode}</span>
            <input id="recurring-amount" type="number" min="0.01" step="0.01" value={amount} onChange={(event) => setAmount(event.target.value)} placeholder="0.00" required />
          </div>
        </div>
        <div className="recurring-field">
          <label htmlFor="recurring-category">Category</label>
          <select id="recurring-category" value={category} onChange={(event) => setCategory(event.target.value)} required>
            {categories.map((name) => <option key={name} value={name}>{name}</option>)}
          </select>
        </div>
        <div className="recurring-field">
          <label htmlFor="recurring-frequency">Repeats</label>
          <select id="recurring-frequency" value={frequency} onChange={(event) => setFrequency(event.target.value)}>
            <option value="WEEKLY">Weekly</option>
            <option value="MONTHLY">Monthly</option>
            <option value="YEARLY">Yearly</option>
          </select>
        </div>
        <div className="recurring-field">
          <label htmlFor="recurring-start-date">First due date</label>
          <input id="recurring-start-date" type="date" value={startDate} onChange={(event) => setStartDate(event.target.value)} required />
        </div>
        <button className="budget-save-button recurring-add-button" type="submit" disabled={saving || !categories.length}>{saving ? 'Saving…' : 'Add schedule'}</button>
      </form>

      {error && <p className="error-message" role="alert">{error}</p>}
      {loading ? <div className="empty-state recurring-empty">Loading recurring expenses…</div> : recurringExpenses.length === 0 ? (
        <div className="empty-state recurring-empty"><strong>No recurring expenses yet</strong><span>Add a schedule above to automatically record regular expenses.</span></div>
      ) : (
        <ul className="recurring-list">
          {recurringExpenses.map((item) => (
            <li className={`recurring-row ${item.active ? '' : 'is-paused'}`} key={item.id}>
              <div className="recurring-main">
                <strong>{item.description}</strong>
                <span>{item.category} · {frequencyLabels[item.frequency]} · {item.active ? `Next ${new Intl.DateTimeFormat(undefined, { month: 'short', day: 'numeric', year: 'numeric' }).format(new Date(`${item.nextDate}T12:00:00`))}` : 'Paused'}</span>
              </div>
              <strong className="recurring-amount">{currency(item.amount, currencyCode)}</strong>
              <div className="recurring-actions">
                <button type="button" onClick={() => toggleActive(item)} disabled={busyId === item.id}>{item.active ? 'Pause' : 'Resume'}</button>
                <button type="button" onClick={() => remove(item)} disabled={busyId === item.id}>Delete</button>
              </div>
            </li>
          ))}
        </ul>
      )}
    </section>
  )
}

export default RecurringExpenseManager
