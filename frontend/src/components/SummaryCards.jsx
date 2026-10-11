const cards = [
  { key: 'totalTransactions', label: 'Total transactions', tone: 'tone-green', marker: '↗' },
  { key: 'totalAlerts', label: 'Total alerts', tone: 'tone-red', marker: '!' },
  { key: 'velocityAlerts', label: 'Velocity alerts', tone: 'tone-blue', marker: '↯' },
  { key: 'aboveMeanAmountAlerts', label: 'Above-mean alerts', tone: 'tone-gold', marker: '≈' },
];

export default function SummaryCards({ data }) {
  return (
    <section className="summary-grid" aria-label="Transaction summary">
      {cards.map((card, index) => (
        <article className={`summary-card ${card.tone}`} key={card.key} style={{ '--card-index': index }}>
          <div className="summary-label">
            <span>{card.label}</span>
            <span className="summary-marker">{card.marker}</span>
          </div>
          <div className="summary-value">{data?.[card.key] ?? '—'}</div>
          <div className="summary-foot">Real-time monitoring status</div>
        </article>
      ))}
    </section>
  );
}