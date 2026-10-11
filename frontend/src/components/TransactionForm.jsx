import { useState } from 'react';
import { requestJson } from '../api.js';

const payerOptions = [
  { id: 'personal@upi', name: 'Personal account' },
  { id: 'joint@upi', name: 'Joint account' },
  { id: 'business@upi', name: 'Business account' },
];

const beneficiaryOptions = [
  { id: 'merchant001@upi', name: 'Grocery store' },
  { id: 'cafe001@upi', name: 'Cafe' },
  { id: 'utility001@upi', name: 'Utilities' },
];

function createTransactionId() {
  const randomPart = Math.random().toString(36).slice(2, 8).toUpperCase();
  const timestampPart = Date.now().toString().slice(-6);
  return `TXN-${timestampPart}-${randomPart}`;
}

function createTransactionPayload(form, amount) {
  return {
    transactionId: form.transactionId.trim(),
    userId: form.userId.trim(),
    beneficiaryId: form.beneficiaryId.trim(),
    amount,
    timestamp: new Date().toISOString(),
  };
}

export default function TransactionForm({ onSubmitted }) {
  const [form, setForm] = useState({
    transactionId: createTransactionId(),
    userId: payerOptions[0].id,
    beneficiaryId: beneficiaryOptions[0].id,
    amount: '100',
  });
  const [busy, setBusy] = useState(false);
  const [message, setMessage] = useState('');
  const [error, setError] = useState('');

  function updateField(event) {
    setForm({ ...form, [event.target.name]: event.target.value });
  }

  async function submitTransaction(event) {
    event.preventDefault();
    setBusy(true);
    setMessage('');
    setError('');

    const cleanAmount = Number(form.amount);
    if (!form.transactionId.trim()) {
      setError('Transaction ID is required.');
      setBusy(false);
      return;
    }
    if (!form.userId.trim()) {
      setError('Please choose a valid payer account.');
      setBusy(false);
      return;
    }
    if (!form.beneficiaryId.trim()) {
      setError('Please choose a valid beneficiary.');
      setBusy(false);
      return;
    }
    if (!Number.isFinite(cleanAmount) || cleanAmount <= 0) {
      setError('Amount must be greater than zero.');
      setBusy(false);
      return;
    }

    try {
      await requestJson('/api/transactions', {
        method: 'POST',
        body: JSON.stringify(createTransactionPayload(form, cleanAmount)),
      });
      setMessage('Transaction accepted and queued for processing.');
      setForm((previous) => ({ ...previous, transactionId: createTransactionId() }));
      onSubmitted();
    } catch (requestError) {
      setError(requestError.message);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section className="panel form-panel">
      <div className="panel-heading">
        <div>
          <p className="eyebrow">PAYMENT</p>
          <h2>Submit transaction</h2>
        </div>
        <span className="panel-step">01</span>
      </div>

      <form className="transaction-form" onSubmit={submitTransaction}>
        <label>
          Transaction ID
          <input
            name="transactionId"
            type="text"
            value={form.transactionId}
            onChange={updateField}
            placeholder="TXN-548201-ABC123"
            required
          />
        </label>

        <div className="field-row">
          <label>
            User ID / Sender
            <select name="userId" value={form.userId} onChange={updateField}>
              {payerOptions.map((payer) => (
                <option key={payer.id} value={payer.id}>
                  {payer.name} · {payer.id}
                </option>
              ))}
            </select>
          </label>

          <label>
            Beneficiary / Receiver
            <select name="beneficiaryId" value={form.beneficiaryId} onChange={updateField}>
              {beneficiaryOptions.map((beneficiary) => (
                <option key={beneficiary.id} value={beneficiary.id}>
                  {beneficiary.name} · {beneficiary.id}
                </option>
              ))}
            </select>
          </label>
        </div>

        <label>
          Amount (INR)
          <input
            name="amount"
            type="number"
            min="0.01"
            step="0.01"
            value={form.amount}
            onChange={updateField}
            placeholder="2500"
            required
          />
        </label>

        {message && <p className="form-message" role="status">{message}</p>}
        {error && <p className="form-error" role="alert">{error}</p>}

        <button className="submit-button" type="submit" disabled={busy}>
          {busy ? 'Processing…' : 'Submit Transaction'}
          <span aria-hidden="true">→</span>
        </button>
      </form>
    </section>
  );
}