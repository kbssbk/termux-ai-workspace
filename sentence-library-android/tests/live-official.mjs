const urls = [
  'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2026년-1월/',
  'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2018년-1월/',
  'https://www.jw.org/ko/라이브러리/magazines/파수대-연구-2016-1/',
  'https://www.jw.org/ko/라이브러리/magazines/w20070101/'
];
for (const url of urls) {
  const r = await fetch(url, {headers:{'user-agent':'Mozilla/5.0 SentenceLibrary-CI','accept-language':'ko-KR,ko;q=0.9'}});
  if (!r.ok) throw new Error(`${url} -> HTTP ${r.status}`);
  const html = await r.text();
  if (!/파수대/.test(html) || !/<h2\b/i.test(html)) throw new Error(`${url} -> expected Korean Watchtower article headings not found`);
  console.log('LIVE PASS', r.status, url);
}
