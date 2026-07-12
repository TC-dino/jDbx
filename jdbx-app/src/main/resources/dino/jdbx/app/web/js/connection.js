/* global jdbx */
(function () {
  const COLORS = ['#EFC524', '#00758F', '#336791', '#DC382D', '#47A248', '#9B59B6', '#E67E22', '#1ABC9C'];
  let selectedColor = COLORS[0];
  let plugins = [];

  function $(id) { return document.getElementById(id); }

  function renderColors() {
    const box = $('colors');
    box.innerHTML = COLORS.map(c =>
      `<span class="color-swatch${c === selectedColor ? ' active' : ''}" data-c="${c}" style="background:${c}"></span>`
    ).join('');
    box.querySelectorAll('.color-swatch').forEach(el => {
      el.onclick = () => {
        selectedColor = el.dataset.c;
        renderColors();
      };
    });
  }

  function renderTypes() {
    const sel = $('type');
    sel.innerHTML = plugins.map(p =>
      `<option value="${p.id}" data-port="${p.defaultPort || 0}">${p.name || p.id}</option>`
    ).join('');
    sel.onchange = () => {
      const opt = sel.selectedOptions[0];
      if (opt && opt.dataset.port && Number(opt.dataset.port) > 0) {
        $('port').value = opt.dataset.port;
      }
    };
    if (sel.options.length) sel.dispatchEvent(new Event('change'));
  }

  function collect() {
    return {
      id: $('id').value || '',
      name: $('name').value.trim(),
      type: $('type').value,
      host: $('host').value.trim(),
      port: parseInt($('port').value || '0', 10) || 0,
      database: $('database').value.trim(),
      username: $('username').value.trim(),
      password: $('password').value,
      color: selectedColor,
      ssl: $('ssl').checked
    };
  }

  function setMsg(text, ok) {
    const el = $('msg');
    el.textContent = text || '';
    el.style.color = ok === true ? '#3ddc97' : (ok === false ? '#e85d5d' : '#8fa3a8');
  }

  window.setPlugins = function (json) {
    try { plugins = typeof json === 'string' ? JSON.parse(json) : json; } catch (e) { plugins = []; }
    renderTypes();
  };

  window.setConnectionForm = function (json) {
    let c = {};
    try { c = typeof json === 'string' ? JSON.parse(json) : (json || {}); } catch (e) { c = {}; }
    $('title').textContent = c.id ? 'Edit Connection' : 'New Connection';
    $('id').value = c.id || '';
    $('name').value = c.name || '';
    $('host').value = c.host || 'localhost';
    $('port').value = c.port || '';
    $('database').value = c.database || '';
    $('username').value = c.username || '';
    $('password').value = c.password || '';
    $('ssl').checked = !!c.ssl;
    if (c.color) selectedColor = c.color;
    renderColors();
    if (c.type) $('type').value = c.type;
  };

  window.setTestResult = function (ok, message) {
    setMsg(message || (ok ? 'Connection OK' : 'Connection failed'), !!ok);
  };

  window.onJdbxReady = function () {
    renderColors();
    $('btnSave').onclick = () => {
      const c = collect();
      if (!c.name || !c.type) {
        setMsg('请填写名称和类型', false);
        return;
      }
      if (window.jdbx) {
        jdbx.saveConnection(JSON.stringify(c));
      }
    };
    $('btnTest').onclick = () => {
      const c = collect();
      if (!c.type) {
        setMsg('请选择类型', false);
        return;
      }
      setMsg('Testing...', null);
      if (window.jdbx) jdbx.testConnection(JSON.stringify(c));
    };
    $('btnCancelTop').onclick = () => window.jdbx && jdbx.closeConnectionDialog();
    if (window.jdbx && jdbx.requestPlugins) jdbx.requestPlugins();
  };

  if (window.jdbx) window.onJdbxReady();
})();
