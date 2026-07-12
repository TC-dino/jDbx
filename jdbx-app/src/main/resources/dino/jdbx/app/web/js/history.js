/* global jdbx */
(function () {
  let rows = [];
  let selected = null;

  function esc(s) {
    return String(s == null ? '' : s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;');
  }

  function render(filter) {
    const q = (filter || '').toLowerCase();
    const filtered = !q ? rows : rows.filter(r => (r.sql || '').toLowerCase().includes(q)
      || (r.connectionId || '').toLowerCase().includes(q));

    const tbody = document.getElementById('tbody');
    const empty = document.getElementById('empty');
    if (!filtered.length) {
      tbody.innerHTML = '';
      empty.classList.remove('d-none');
      return;
    }
    empty.classList.add('d-none');
    tbody.innerHTML = filtered.map((r, idx) => {
      const sql = (r.sql || '').replace(/\s+/g, ' ');
      const short = sql.length > 80 ? sql.slice(0, 80) + '…' : sql;
      const ok = r.success ? '✓' : '✗';
      return `<tr data-idx="${idx}" style="cursor:pointer">
        <td>${esc(r.executedAt || '')}</td>
        <td>${esc(r.connectionName || r.connectionId || '')}</td>
        <td title="${esc(sql)}">${esc(short)}</td>
        <td>${ok}</td>
      </tr>`;
    }).join('');

    // map filtered indices carefully
    const map = filtered;
    tbody.querySelectorAll('tr').forEach((tr, i) => {
      tr.onclick = () => {
        selected = map[i];
        document.getElementById('detail').textContent = selected.sql || '';
        document.getElementById('btnReplay').disabled = !selected;
        tbody.querySelectorAll('tr').forEach(x => x.classList.remove('table-active'));
        tr.classList.add('table-active');
      };
      tr.ondblclick = () => {
        selected = map[i];
        if (window.jdbx && selected) jdbx.replay(selected.sql || '');
      };
    });
  }

  window.renderHistory = function (json) {
    try {
      rows = typeof json === 'string' ? JSON.parse(json) : (json || []);
    } catch (e) {
      rows = [];
    }
    selected = null;
    document.getElementById('btnReplay').disabled = true;
    document.getElementById('detail').textContent = '选择一条历史记录';
    render(document.getElementById('q').value);
  };

  window.onJdbxReady = function () {
    document.getElementById('btnSearch').onclick = () => render(document.getElementById('q').value);
    document.getElementById('q').onkeydown = (e) => {
      if (e.key === 'Enter') render(document.getElementById('q').value);
    };
    document.getElementById('btnClear').onclick = () => window.jdbx && jdbx.clearHistory();
    document.getElementById('btnReplay').onclick = () => {
      if (window.jdbx && selected) jdbx.replay(selected.sql || '');
    };
    document.getElementById('btnClose').onclick = () => window.jdbx && jdbx.close();
    if (window.jdbx && jdbx.requestHistory) jdbx.requestHistory();
  };

  if (window.jdbx) window.onJdbxReady();
})();
