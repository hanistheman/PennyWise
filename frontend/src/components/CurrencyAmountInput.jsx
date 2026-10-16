function CurrencyAmountInput({ id, name, value, onChange, min = '0.01', step = '0.01', placeholder = '0.00', required = true }) {
  return (
    <div className="amount-input">
      <span aria-hidden="true">$</span>
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
