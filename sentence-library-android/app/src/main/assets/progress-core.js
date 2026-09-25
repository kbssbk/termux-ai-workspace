(function(root,factory){const api=factory();if(typeof module==='object'&&module.exports)module.exports=api;if(root)root.ProgressCore=api})(typeof globalThis!=='undefined'?globalThis:this,function(){
  function clampProgress(v){v=Number(v);if(!Number.isFinite(v))v=0;return Math.max(0,Math.min(100,Math.round(v)))}
  function nextProgressForStatus(status,current){if(status==='미완')return 0;if(status==='완성')return 100;const p=clampProgress(current);return p<=0||p>=100?25:p}
  function deriveIssueProgress(articles){if(!Array.isArray(articles)||!articles.length)return 0;return Math.round(articles.reduce((s,a)=>s+clampProgress(a.progress),0)/articles.length)}
  function deriveIssueStatus(articles){if(!Array.isArray(articles)||!articles.length)return '미완';const p=deriveIssueProgress(articles);if(p<=0)return '미완';if(p>=100&&articles.every(a=>a.status==='완성'||clampProgress(a.progress)===100))return '완성';return '진행중'}
  return {clampProgress,nextProgressForStatus,deriveIssueProgress,deriveIssueStatus};
});
