/* Carnet de voyage: works offline once installed. Network first for the app itself (to get updates), cache as fallback. */
const CACHE='carnet-v5';
const ASSETS=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png','./icon-maskable.png'];
self.addEventListener('install',e=>{e.waitUntil(caches.open(CACHE).then(c=>c.addAll(ASSETS)).then(()=>self.skipWaiting()))});
self.addEventListener('activate',e=>{e.waitUntil(caches.keys().then(ks=>Promise.all(ks.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim()))});
self.addEventListener('fetch',e=>{
  const u=new URL(e.request.url);
  if(e.request.method!=='GET'||u.origin!==location.origin)return;
  e.respondWith(networkFirst(e.request));
});
async function networkFirst(req){
  const cache=await caches.open(CACHE);
  try{
    const res=await Promise.race([fetch(req,{cache:'no-cache'}),new Promise((_,rej)=>setTimeout(()=>rej(new Error('timeout')),4000))]);
    if(res&&res.ok){cache.put(req,res.clone());return res}
    /* site unreachable or page missing: keep using the copy installed on the phone */
    const hit=await cache.match(req,{ignoreSearch:true})||(req.mode==='navigate'?await cache.match('./index.html'):null);
    return hit||res;
  }catch(err){
    const hit=await cache.match(req,{ignoreSearch:true})||(req.mode==='navigate'?await cache.match('./index.html'):null);
    if(hit)return hit;
    throw err;
  }
}
