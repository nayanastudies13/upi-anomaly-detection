function formatAmount(value) {
  return new Intl.NumberFormat('en-IN', {
    style: 'currency',
    currency: 'INR',
    maximumFractionDigits: 2,
  }).format(value);
}

function formatTimestamp(value) {
  return new Date(value).toLocaleString('en-IN', {
    dateStyle: 'medium',
    timeStyle: 'short',
  });
}

function getStatusClass(status) {
  switch (status) {
    case 'ALERT':
      return 'status-alert';
    case 'PROCESSING':
      return 'status-processing';
    default:
      return 'status-normal';
  }
}

export default function TransactionTable({ transactions }) {
  return (
    <section className="panel table-panel">
      <div className="panel-heading compact">
        <div>
          <p className="eyebrow">LATEST ACTIVITY</p>
          <h2>Recent transactions</h2>
        </div>
        <span className="table-count">{transactions.length} records</span>
      </div>

      {transactions.length === 0 ? (
        <div className="empty-state">No transactions have been submitted yet.</div>
      ) : (
        <div className="table-scroll">
          <table>
            <thead>
              <tr>
                <th>Transaction</th>
                <th>User</th>
                <th>Beneficiary</th>
                <th>Amount</th>
                <th>Timestamp</th>
                <th>Status</th>
              </tr>
            </thead>
            <tbody>
              {transactions.map((transaction) => (
                <tr key={transaction.transactionId}>
                  <td className="mono-cell">{transaction.transactionId}</td>
                  <td>{transaction.userId}</td>
                  <td>{transaction.beneficiaryId}</td>
                  <td className="amount-cell">{formatAmount(transaction.amount)}</td>
                  <td className="time-cell">{formatTimestamp(transaction.timestamp)}</td>
                  <td>
                    <span className={`status-pill ${getStatusClass(transaction.status)}`}>
                      {transaction.status}
                    </span>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
}