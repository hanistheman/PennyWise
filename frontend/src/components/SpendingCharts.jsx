import { useMemo } from 'react'

const monthLabel = new Intl.DateTimeFormat('en-US', { month: 'short' })

function getMonthKey(date) {
  return `${date.getFullYear()}-${String(date.getMonth() + 1).padStart(2, '0')}`
}

function SpendingCharts({ expenses, categories, currentMonth, currencyCode = 'USD' }) {
  const currency = new Intl.NumberFormat(undefined, { style: 'currency', currency: currencyCode, maximumFractionDigits: 0 })
  const { categoryTotals, monthlyTotals } = useMemo(() => {
    const currentMonthExpenses = expenses.filter((expense) => expense.spentOn.startsWith(currentMonth))
    const categoryAmounts = new Map(categories.map((category) => [category, 0]))

    currentMonthExpenses.forEach((expense) => {
      categoryAmounts.set(
        expense.category,
        (categoryAmounts.get(expense.category) || 0) + Number(expense.amount),
      )
    })

    const [year, month] = currentMonth.split('-').map(Number)
    const months = Array.from({ length: 6 }, (_, index) => {
      const date = new Date(year, month - 6 + index, 1)
      return { key: getMonthKey(date), label: monthLabel.format(date) }
    })
    const totals = new Map(months.map(({ key }) => [key, 0]))

    expenses.forEach((expense) => {
      const monthKey = expense.spentOn.slice(0, 7)
      if (totals.has(monthKey)) {
        totals.set(monthKey, totals.get(monthKey) + Number(expense.amount))
      }
    })

    return {
      categoryTotals: [...categoryAmounts.entries()]
        .map(([category, amount]) => ({ category, amount }))
        .sort((a, b) => b.amount - a.amount || a.category.localeCompare(b.category)),
      monthlyTotals: months.map((month) => ({ ...month, amount: totals.get(month.key) })),
    }
  }, [expenses, categories, currentMonth])

  const categoryMaximum = Math.max(...categoryTotals.map(({ amount }) => amount), 0)
  const monthlyMaximum = Math.max(...monthlyTotals.map(({ amount }) => amount), 0)
  const currentMonthTotal = monthlyTotals.at(-1)?.amount || 0

  return (
    <section className="spending-insights" aria-labelledby="spending-insights-title">
      <div className="spending-insights-heading">
        <div>
          <p className="eyebrow">YOUR SPENDING, VISUALIZED</p>
          <h2 id="spending-insights-title">Spending insights</h2>
        </div>
        <span className="insights-period">Updated as expenses change</span>
      </div>

      <div className="spending-chart-grid">
        <article className="panel spending-chart-card">
          <div className="spending-chart-heading">
            <div>
              <h3>By category</h3>
              <p>This month · {currency.format(currentMonthTotal)} total</p>
            </div>
            <span className="chart-icon" aria-hidden="true">◒</span>
          </div>
          {currentMonthTotal === 0 ? (
            <p className="chart-empty">Add an expense this month to see your category breakdown.</p>
          ) : (
            <div className="category-chart" aria-label="Current month spending by category">
              {categoryTotals.map(({ category, amount }) => {
                const percent = categoryMaximum ? Math.round((amount / categoryMaximum) * 100) : 0
                return (
                  <div className="category-chart-row" key={category}>
                    <div className="category-chart-label">
                      <span>{category}</span>
                      <strong>{currency.format(amount)}</strong>
                    </div>
                    <div
                      className="category-chart-track"
                      role="progressbar"
                      aria-label={`${category} spending`}
                      aria-valuemin="0"
                      aria-valuemax="100"
                      aria-valuenow={percent}
                    >
                      <div className="category-chart-fill" style={{ width: `${percent}%` }} />
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </article>

        <article className="panel spending-chart-card">
          <div className="spending-chart-heading">
            <div>
              <h3>Monthly trend</h3>
              <p>Total spending over the last six months</p>
            </div>
            <span className="chart-icon" aria-hidden="true">▥</span>
          </div>
          {monthlyMaximum === 0 ? (
            <p className="chart-empty">Your monthly spending trend will appear here once you add expenses.</p>
          ) : (
            <div className="trend-chart" role="img" aria-label={`Monthly spending for the last six months: ${monthlyTotals.map(({ label, amount }) => `${label} ${currency.format(amount)}`).join(', ')}`}>
              {monthlyTotals.map(({ key, label, amount }) => {
                const height = monthlyMaximum ? Math.max((amount / monthlyMaximum) * 116, amount > 0 ? 4 : 0) : 0
                return (
                  <div className="trend-chart-column" key={key} title={`${label}: ${currency.format(amount)}`}>
                    <span className="trend-chart-value">{amount > 0 ? currency.format(amount) : '—'}</span>
                    <div className="trend-chart-bar-area">
                      <div className="trend-chart-bar" style={{ height: `${height}px` }} />
                    </div>
                    <span className="trend-chart-label">{label}</span>
                  </div>
                )
              })}
            </div>
          )}
        </article>
      </div>
    </section>
  )
}

export default SpendingCharts
