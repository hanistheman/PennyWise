import { useState } from 'react'

export default function AuthPage({ onAuthenticated }) {
  const [mode, setMode] = useState('login')
  const [email, setEmail] = useState('')
  const [password, setPassword] = useState('')
  const [error, setError] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const isSignup = mode === 'signup'

  async function submit(event) {
    event.preventDefault()
    setError('')
    setSubmitting(true)
    try {
      const response = await fetch(`/api/auth/${isSignup ? 'register' : 'login'}`, {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        body: JSON.stringify({ email, password }),
      })
      const body = await response.json().catch(() => ({}))
      if (!response.ok) throw new Error(body.message || 'Could not sign in. Please try again.')
      onAuthenticated(body)
    } catch (requestError) {
      setError(requestError.message || 'Could not reach the server. Please try again.')
    } finally {
      setSubmitting(false)
    }
  }

  return (
    <main className="auth-page">
      <section className="auth-card" aria-labelledby="auth-title">
        <a className="brand auth-brand" href="/" aria-label="PennyWise home">
          <span className="brand-mark">P</span><span>PennyWise</span>
        </a>
        <p className="eyebrow auth-eyebrow">YOUR MONEY, IN FOCUS</p>
        <h1 id="auth-title">{isSignup ? 'Create your account' : 'Welcome back'}</h1>
        <p className="auth-subtitle">{isSignup ? 'Start building a clearer picture of your spending.' : 'Sign in to see your spending at a glance.'}</p>

        <div className="auth-tabs" role="tablist" aria-label="Account access">
          <button type="button" role="tab" aria-selected={!isSignup} className={!isSignup ? 'active' : ''} onClick={() => { setMode('login'); setError('') }}>Log in</button>
          <button type="button" role="tab" aria-selected={isSignup} className={isSignup ? 'active' : ''} onClick={() => { setMode('signup'); setError('') }}>Sign up</button>
        </div>

        <form className="auth-form" onSubmit={submit}>
          <label htmlFor="auth-email">Email</label>
          <input id="auth-email" type="email" autoComplete="email" required maxLength={254} value={email} onChange={(event) => setEmail(event.target.value)} placeholder="you@example.com" />
          <label htmlFor="auth-password">Password</label>
          <input id="auth-password" type="password" autoComplete={isSignup ? 'new-password' : 'current-password'} required minLength={8} maxLength={72} value={password} onChange={(event) => setPassword(event.target.value)} placeholder="At least 8 characters" />
          {error && <p className="auth-error" role="alert">{error}</p>}
          <button className="auth-submit" type="submit" disabled={submitting}>
            {submitting ? 'Please wait…' : isSignup ? 'Create account' : 'Log in'}
            {!submitting && <span aria-hidden="true">→</span>}
          </button>
        </form>
        <p className="auth-note">Your account keeps your finances ready whenever you return.</p>
      </section>
    </main>
  )
}
