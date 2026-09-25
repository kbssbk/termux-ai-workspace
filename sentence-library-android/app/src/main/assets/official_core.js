(function(root,factory){
  const api=factory();
  if(typeof module==='object'&&module.exports) module.exports=api;
  if(root) root.OFFICIAL_CORE=api;
})(typeof globalThis!=='undefined'?globalThis:this,function(){
  const EXCLUDED = new Set([
    '여호와의 증인','JW.ORG','다운로드 옵션','공유','목차','이전','다음',
    '텍스트 출판물 다운로드 옵션','오디오 다운로드 옵션','오디오','한국어'
  ]);

  function generateIssueCandidates(maxYear=2026,minYear=2001){
    const out=[];
    for(let y=maxYear;y>=minYear;y--){
      for(let m=1;m<=12;m++){
        const mm=String(m).padStart(2,'0');
        if(y>=2016){
          out.push({year:y,month:m,day:null,code:`${y}${mm}`});
        } else if(y>=2008){
          out.push({year:y,month:m,day:15,code:`${y}${mm}15`});
        } else {
          out.push({year:y,month:m,day:1,code:`${y}${mm}01`});
          out.push({year:y,month:m,day:15,code:`${y}${mm}15`});
        }
      }
    }
    return out;
  }

  function issueLabel(code){
    const s=String(code),m=Number(s.slice(4,6));
    if(s.length>=8){ const d=Number(s.slice(6,8)); return `${m}월 ${d}일호`; }
    return `${m}월호`;
  }

  function issueUrlCandidates(candidate){
    const y=Number(candidate.year), m=Number(candidate.month), code=String(candidate.code||'');
    const base='https://www.jw.org/ko/라이브러리/magazines/';
    if(y>=2018) return [`${base}파수대-연구-${y}년-${m}월/`];
    if(y>=2016) return [`${base}파수대-연구-${y}-${m}/`];
    return [`${base}w${code}/`];
  }

  function issueUrl(codeOrCandidate){
    if(codeOrCandidate && typeof codeOrCandidate==='object') return issueUrlCandidates(codeOrCandidate)[0];
    const code=String(codeOrCandidate||'');
    const year=Number(code.slice(0,4)), month=Number(code.slice(4,6)), day=code.length>=8?Number(code.slice(6,8)):null;
    return issueUrlCandidates({year,month,day,code})[0];
  }

  function decodeEntities(s){
    const named={amp:'&',lt:'<',gt:'>',quot:'"',apos:"'",nbsp:' '};
    return String(s||'').replace(/&(#x?[0-9a-f]+|[a-z]+);/gi,(m,x)=>{
      if(x[0]==='#'){
        const hex=x[1]?.toLowerCase()==='x'; const n=parseInt(x.slice(hex?2:1),hex?16:10);
        return Number.isFinite(n)?String.fromCodePoint(n):m;
      }
      return Object.prototype.hasOwnProperty.call(named,x.toLowerCase())?named[x.toLowerCase()]:m;
    });
  }

  function cleanText(s){
    return decodeEntities(String(s||'')
      .replace(/<script[\s\S]*?<\/script>/gi,' ')
      .replace(/<style[\s\S]*?<\/style>/gi,' ')
      .replace(/<[^>]+>/g,' '))
      .replace(/\s+/g,' ').trim();
  }

  function isGoodTitle(t){
    if(!t||t.length<2||t.length>220||EXCLUDED.has(t)) return false;
    if(/^다운로드\s*옵션/.test(t)||/^오디오\s/.test(t)||/^텍스트\s/.test(t)) return false;
    return true;
  }

  function absoluteUrl(href,baseUrl){
    try{return decodeURI(new URL(href,baseUrl).href)}catch{return ''}
  }

  function extractArticles(html,baseUrl){
    let src=String(html||'');
    const main=src.match(/<main\b[^>]*>([\s\S]*?)<\/main>/i); if(main) src=main[1];
    const out=[],seen=new Set();
    const anchorRe=/<a\b([^>]*)>([\s\S]*?)<\/a>/gi; let a;
    while((a=anchorRe.exec(src))){
      const inner=a[2];
      const h2=(inner.match(/<h2\b[^>]*>([\s\S]*?)<\/h2>/i)||[])[1];
      if(!h2) continue;
      const title=cleanText(h2);
      if(!isGoodTitle(title)) continue;
      const href=(a[1].match(/\bhref\s*=\s*["']([^"']+)["']/i)||[])[1]||'';
      const key=normalizeTitle(title);
      if(seen.has(key)) continue;
      seen.add(key);
      out.push({title,sourceUrl:absoluteUrl(href,baseUrl)||baseUrl||''});
    }
    if(out.length) return out;

    const re=/<h2\b[^>]*>([\s\S]*?)<\/h2>/gi; let m;
    while((m=re.exec(src))){
      const title=cleanText(m[1]);
      if(!isGoodTitle(title)) continue;
      const key=normalizeTitle(title);if(seen.has(key))continue;seen.add(key);
      out.push({title,sourceUrl:baseUrl||''});
    }
    return out;
  }

  function extractArticleTitles(html){ return extractArticles(html,'').map(x=>x.title); }
  function normalizeTitle(s){return String(s||'').normalize('NFKC').replace(/[“”‘’'"\s]/g,'').toLowerCase()}

  function mergeOfficialIssue(state,data,uid){
    state.issues ||= []; state.quotes ||= [];
    let issue=state.issues.find(i=>i.officialCode===data.code);
    if(!issue){
      issue={id:uid('issue'),officialCode:data.code,official:true,year:data.year,publication:'파수대—연구용',label:data.label||issueLabel(data.code),sourceUrl:data.sourceUrl||issueUrl(data),sourceHost:'jw.org',articles:[],createdAt:0,updatedAt:0};
      state.issues.push(issue);
    }
    issue.official=true; issue.year=data.year; issue.publication='파수대—연구용'; issue.label=data.label||issueLabel(data.code); issue.sourceUrl=data.sourceUrl||issueUrl(data); issue.sourceHost='jw.org'; issue.syncedAt=Date.now();
    const byTitle=new Map((issue.articles||[]).map(a=>[normalizeTitle(a.title),a]));
    const incoming=(data.articles&&data.articles.length)?data.articles:(data.titles||[]).map(title=>({title,sourceUrl:issue.sourceUrl}));
    for(const item of incoming){
      const title=typeof item==='string'?item:item.title;
      const sourceUrl=(typeof item==='object'&&item.sourceUrl)||issue.sourceUrl;
      const key=normalizeTitle(title); if(!key) continue;
      const existing=byTitle.get(key);
      if(existing){ existing.official=true; existing.sourceUrl=sourceUrl; continue; }
      const a={id:uid('art'),title,status:'미완',progress:0,updatedAt:0,official:true,sourceUrl};
      issue.articles.push(a);byTitle.set(key,a);
    }
    issue.contentSyncedAt=Date.now();
    return issue;
  }

  return {generateIssueCandidates,issueLabel,issueUrl,issueUrlCandidates,extractArticles,extractArticleTitles,mergeOfficialIssue,normalizeTitle,cleanText};
});
