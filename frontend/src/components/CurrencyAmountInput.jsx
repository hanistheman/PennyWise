function CurrencyAmountInput({ id, name, value, onChange, currencyCode = 'USD', min = '0.01', step = '0.01', placeholder = '0.00', required = true }) {
  const currencySymbol = new Intl.NumberFormat(undefined, { style: 'currency', currency: currencyCode })
    .formatToParts(0).find((part) => part.type === 'currency')?.value || currencyCode
  return (
    <div className="amount-input">
      <span aria-hidden="true">{currencySymbol}</span>
      <input
        id={id}
        name={name}
        type="number"
        min={min}
        step={step}
        placeholder={placeholder}
        value={value}
        onChange={onChange}
        required={required}
      />
    </div>
  )
}

export default CurrencyAmountInput
