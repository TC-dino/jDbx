/* global jdbx */
(function () {
  let connections = [];
  let filter = '';
  let selectedId = null;

  function esc(s) {
    return String(s == null ? '' : s)
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function subtitle(c) {
    if ((c.type || '').toLowerCase() === 'sqlite') {
      return c.database || 'sqlite';
    }
    const host = c.host || 'localhost';
    const port = c.port ? (':' + c.port) : '';
    const db = c.database ? ('/' + c.database) : '';
    return host + port + db;
  }

  function statusClass(c) {
    if (c.status === 'error') return 'err';
    if (c.status === 'connected') return 'on';
    return 'off';
  }

  function matches(c) {
    if (!filter) return true;
    const q = filter.toLowerCase();
    return [c.name, c.host, c.type, c.database].some(v => (v || '').toLowerCase().includes(q));
  }

  function renderEntities(c) {
    if (!c.entities || !c.entities.length) return '';
    let html = '<div class="bk-entities">';
    c.entities.forEach(group => {
      html += `<div class="bk-folder">${esc(group.label)}</div>`;
      (group.items || []).forEach(item => {
        const icon = item.kind === 'view' ? '◫' : '▦';
        html += `<div class="bk-entity" data-conn="${esc(c.id)}" data-table="${esc(item.name)}" data-kind="${esc(item.kind || 'table')}">
          <span class="bk-entity-icon">${icon}</span><span>${esc(item.name)}</span>
        </div>`;
      });
    });
    html += '</div>';
    return html;
  }

  function render() {
    const list = document.getElementById('list');
    const label = document.getElementById('savedLabel');
    const filtered = connections.filter(matches);
    label.textContent = 'SAVED  ' + connections.length;

    if (!filtered.length) {
      list.innerHTML = '<div class="bk-empty">暂无保存的连接<br/>点击上方按钮开始</div>';
      return;
    }

    list.innerHTML = filtered.map(c => {
      const active = c.id === selectedId ? ' active' : '';
      return `<div class="bk-conn${active}" data-id="${esc(c.id)}" tabindex="0">
        <div class="bk-stripe" style="background:${esc(c.color || '#efc524')}"></div>
        <div class="bk-conn-body">
          <div class="bk-conn-title"><span class="bk-status-dot ${statusClass(c)}"></span>${esc(c.name)}</div>
          <div class="bk-conn-sub">${esc(subtitle(c))}</div>
        </div>
        <span class="bk-pill">${esc((c.type || 'db').toLowerCase())}</span>
      </div>${c.expanded ? renderEntities(c) : ''}`;
    }).join('');

    list.querySelectorAll('.bk-conn').forEach(el => {
      el.addEventListener('click', () => {
        selectedId = el.dataset.id;
        render();
      });
      el.addEventListener('dblclick', () => {
        if (window.jdbx) jdbx.connect(el.dataset.id);
      });
      el.addEventListener('contextmenu', (e) => {
        e.preventDefault();
        if (window.jdbx) jdbx.connectionContext(el.dataset.id);
      });
    });

    list.querySelectorAll('.bk-entity').forEach(el => {
      el.addEventListener('dblclick', () => {
        if (window.jdbx) jdbx.openTable(el.dataset.conn, el.dataset.table, el.dataset.kind || 'table');
      });
      el.addEventListener('click', (e) => e.stopPropagation());
    });
  }

  window.renderConnections = function (json) {
    try {
      connections = typeof json === 'string' ? JSON.parse(json) : (json || []);
    } catch (e) {
      connections = [];
    }
    render();
  };

  window.onJdbxReady = function () {
    document.getElementById('btnNew').onclick = () => window.jdbx && jdbx.newConnection();
    document.getElementById('filter').oninput = (e) => {
      filter = e.target.value || '';
      render();
    };
    if (window.jdbx && jdbx.requestConnections) {
      jdbx.requestConnections();
    }
  };

  // If bridge already present
  if (window.jdbx) {
    window.onJdbxReady();
  }
})();
