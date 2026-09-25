const assert = require('assert');
const CORE = require('../app/src/main/assets/official_core.js');

function test(name, fn){
  try { fn(); console.log('PASS', name); }
  catch (e) { console.error('FAIL', name); console.error(e.stack || e); process.exitCode = 1; }
}

test('builds modern Korean JW.ORG issue URL for 2026', () => {
  const c = {year:2026,month:1,code:'202601'};
  assert.deepStrictEqual(CORE.issueUrlCandidates(c), [
    'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2026년-1월/'
  ]);
});

test('builds legacy slug JW.ORG issue URL for 2017', () => {
  const c = {year:2017,month:1,code:'201701'};
  assert.deepStrictEqual(CORE.issueUrlCandidates(c), [
    'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2017-1/'
  ]);
});

test('builds w-code URL for 2015 and 2007', () => {
  assert.deepStrictEqual(CORE.issueUrlCandidates({year:2015,month:1,day:15,code:'20150115'}), [
    'https://www.jw.org/ko/라이브러리/magazines/w20150115/'
  ]);
  assert.deepStrictEqual(CORE.issueUrlCandidates({year:2007,month:1,day:1,code:'20070101'}), [
    'https://www.jw.org/ko/라이브러리/magazines/w20070101/'
  ]);
});

test('extracts article titles and canonical links from h2 anchors', () => {
  const html = `<html><main>
    <a href="/ko/라이브러리/magazines/파수대-연구-2026년-1월/영적-필요/"><div><h2>“영적 필요”를 채우기 위해 계속 노력하십시오</h2></div></a>
    <a href="/ko/라이브러리/magazines/파수대-연구-2026년-1월/대속/"><h2>왜 우리에게 대속이 필요합니까?</h2></a>
    <h2>다운로드 옵션</h2>
  </main></html>`;
  assert.deepStrictEqual(CORE.extractArticles(html, 'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2026년-1월/'), [
    {title:'“영적 필요”를 채우기 위해 계속 노력하십시오', sourceUrl:'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2026년-1월/영적-필요/'},
    {title:'왜 우리에게 대속이 필요합니까?', sourceUrl:'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2026년-1월/대속/'}
  ]);
});

test('merge preserves progress/status while refreshing official article link', () => {
  const state = {issues:[{id:'i1',officialCode:'202601',articles:[{id:'a1',title:'왜 우리에게 대속이 필요합니까?',status:'진행중',progress:42,sourceUrl:'old'}]}],quotes:[]};
  let n=0;
  CORE.mergeOfficialIssue(state,{code:'202601',year:2026,label:'1월호',sourceUrl:'https://www.jw.org/x',articles:[{title:'왜 우리에게 대속이 필요합니까?',sourceUrl:'https://www.jw.org/new'}]},()=>`id${++n}`);
  assert.strictEqual(state.issues[0].articles[0].progress,42);
  assert.strictEqual(state.issues[0].articles[0].status,'진행중');
  assert.strictEqual(state.issues[0].articles[0].sourceUrl,'https://www.jw.org/new');
});
