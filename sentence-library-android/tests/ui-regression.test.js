const assert = require('assert');
const fs = require('fs');
const path = require('path');

const html = fs.readFileSync(path.join(__dirname, '../app/src/main/assets/index.html'), 'utf8');

function test(name, fn){
  try{ fn(); console.log('PASS', name); }
  catch(e){ console.error('FAIL', name); throw e; }
}

test('issue summary renders as an explicit tap target with accessible action', () => {
  assert.match(html, /class="issueSummary tapTarget"/);
  assert.match(html, /role="button"/);
  assert.match(html, /const aria=expandable\?'기사별 진행률 열기':'호수 진행 보기'/);
  assert.match(html, /aria-label="\$\{aria\}"/);
  assert.match(html, /탭해서 기사별 % 조정/);
});

test('dashboard screen and navigation entry exist', () => {
  assert.match(html, /id="dashboardScreen"/);
  assert.match(html, /data-screen="dashboard"/);
  assert.match(html, /id="dashboardYearList"/);
  assert.match(html, /renderDashboard\(\)/);
});
