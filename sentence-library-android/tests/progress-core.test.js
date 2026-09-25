const assert = require('assert');
const CORE = require('../app/src/main/assets/progress-core.js');

function test(name, fn){
  try{ fn(); console.log('PASS', name); }
  catch(e){ console.error('FAIL', name); throw e; }
}

test('dashboard metrics summarize article states and average progress', () => {
  const issues = [
    {year:2026, articles:[
      {status:'완성', progress:100},
      {status:'진행중', progress:40},
      {status:'미완', progress:0}
    ]},
    {year:2025, articles:[
      {status:'완성', progress:100}
    ]}
  ];
  assert.deepStrictEqual(CORE.dashboardMetrics(issues), {
    total:4, done:2, inProgress:1, unfinished:1, overallProgress:60
  });
});

test('year dashboard metrics are newest first and preserve totals', () => {
  const issues = [
    {year:2025, articles:[{status:'완성', progress:100},{status:'미완', progress:0}]},
    {year:2026, articles:[{status:'진행중', progress:50},{status:'완성', progress:100}]}
  ];
  assert.deepStrictEqual(CORE.yearMetrics(issues), [
    {year:2026,total:2,done:1,inProgress:1,unfinished:0,progress:75},
    {year:2025,total:2,done:1,inProgress:0,unfinished:1,progress:50}
  ]);
});
