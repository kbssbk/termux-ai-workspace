import { createRequire } from 'node:module';
const require = createRequire(import.meta.url);
const CORE = require('../app/src/main/assets/official_core.js');

const candidates = [
  {year:2026,month:1,code:'202601'},
  {year:2018,month:1,code:'201801'},
  {year:2016,month:1,code:'201601'},
  {year:2007,month:1,day:1,code:'20070101'}
];
for (const candidate of candidates) {
  const url = CORE.issueUrlCandidates(candidate)[0];
  const r = await fetch(url, {headers:{'user-agent':'Mozilla/5.0 SentenceLibrary-CI','accept-language':'ko-KR,ko;q=0.9'}});
  if (!r.ok) throw new Error(`${url} -> HTTP ${r.status}`);
  const html = await r.text();
  if (!/파수대/.test(html)) throw new Error(`${url} -> Korean Watchtower marker not found`);
  const articles = CORE.extractArticles(html, url);
  if (articles.length < 3) throw new Error(`${url} -> only ${articles.length} article links extracted`);
  if (articles.some(a => !/^https:\/\/www\.jw\.org\//.test(a.sourceUrl))) throw new Error(`${url} -> non-JW article link extracted`);
  console.log('LIVE PASS', r.status, articles.length, url);
}
