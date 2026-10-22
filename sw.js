/* Carnet de voyage: works offline once installed.
   Network first for the app itself (to get updates), installed copy as fallback.
   A slow download is never wasted: it keeps going in the background and replaces the installed copy when it finishes. */
const CACHE='carnet-v13';
const ASSETS=['./','./index.html','./manifest.webmanifest','./icon-192.png','./icon-512.png','./icon-maskable.png'];
self.addEventListener('install',e=>{
  e.waitUntil(caches.open(CACHE).then(c=>c.addAll(ASSETS.map(u=>new Request(u,{cache:'reload'})))).then(()=>self.skipWaiting()));
});
self.addEventListener('activate',e=>{
  e.waitUntil(caches.keys().then(ks=>Promise.all(ks.filter(k=>k!==CACHE).map(k=>caches.delete(k)))).then(()=>self.clients.claim()));
});
self.addEventListener('fetch',e=>{
  const u=new URL(e.request.url);
  if(e.request.method!=='GET'||u.origin!==location.origin)return;
  e.respondWith(networkFirst(e));
});
async function fromCache(cache,req){
  return await cache.match(req,{ignoreSearch:true})||(req.mode==='navigate'?await cache.match('./index.html'):null);
}
async function networkFirst(e){
  const req=e.request,cache=await caches.open(CACHE);
  const net=fetch(req,{cache:'no-cache'}).then(res=>{
    if(res&&res.ok){
      const copy=res.clone();
      cache.put(req,copy);
      if(req.mode==='navigate')cache.put('./index.html',res.clone());
    }
    return res;
  });
  e.waitUntil(net.catch(()=>{}));
  try{
    const res=await Promise.race([net,new Promise((_,rej)=>setTimeout(()=>rej(new Error('timeout')),6000))]);
    if(res&&res.ok)return res;
    return (await fromCache(cache,req))||res;
  }catch(err){
    const hit=await fromCache(cache,req);
    if(hit)return hit;
    return net;
  }
}
