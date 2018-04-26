var clusterMaker = require('clusters');
var fs = require("fs");

const CHANNEL_SUB_RATE = 16;
const K = 64;

//create single channel bins
let channelBins = [];
let i = 0;
let id = 0;
while(i < 256){
    var obj = {
        id: id,
        range: [i, i + CHANNEL_SUB_RATE - 1],
        center: Math.floor((i + i + CHANNEL_SUB_RATE - 1) / 2)
    };
    channelBins.push(obj);
    i = i + CHANNEL_SUB_RATE;
    id = id + 1;
}

//convert 16*16*16 RGBs to HSVs
let HSVs = [];
for(r of channelBins){
    for(g of channelBins){
        for(b of channelBins){
            let temp = rgbToHsv(r.center, g.center, b.center);
            HSVs.push(temp);
        }
    }
}

//K-means cluster HSV
//number of clusters, defaults to undefined
clusterMaker.k(K);
//number of iterations (higher number gives more time to converge), defaults to 1000
// clusterMaker.iterations(1);
//data from which to identify clusters, defaults to []
clusterMaker.data(HSVs);
let result = clusterMaker.clusters();

//give clusters bin id
i = 0;
for(var cluster of result){
    cluster.id = i;
    i ++;
}

//write the map file: RGB bins' id -> HSV bins' id
let map = [];
for(r of channelBins){
    for(g of channelBins){
        for(b of channelBins){
            let temp = rgbToHsv(r.center, g.center, b.center);
            let cluster_id = findCluster(temp);
            map.push({
                rgb_id: [r.id, g.id, b.id],
                hsv_id: cluster_id
            });
        }
    }
}
fs.writeFile("./map.json", JSON.stringify(map, null), (err) => {
    if (err) {
        console.error(err);
        return;
    };
    console.log("Map File has been created");
});

//find cluster id
function findCluster(hsv){
    for(let cluster of result){
        for(let point of cluster.points){
            if(hsv.toString() == point.toString())
                return cluster.id;
        }
    }
    console.log('find stranger!');
}

/**
 * Converts an RGB color value to HSV. Conversion formula
 * adapted from http://en.wikipedia.org/wiki/HSV_color_space.
 * Assumes r, g, and b are contained in the set [0, 255] and
 * returns h, s, and v in the set [0, 1].
 *
 * @param   Number  r       The red color value
 * @param   Number  g       The green color value
 * @param   Number  b       The blue color value
 * @return  Array           The HSV representation
 */
function rgbToHsv(r, g, b) {
    r /= 255, g /= 255, b /= 255;
  
    var max = Math.max(r, g, b), min = Math.min(r, g, b);
    var h, s, v = max;
  
    var d = max - min;
    s = max == 0 ? 0 : d / max;
  
    if (max == min) {
      h = 0; // achromatic
    } else {
      switch (max) {
        case r: h = (g - b) / d + (g < b ? 6 : 0); break;
        case g: h = (b - r) / d + 2; break;
        case b: h = (r - g) / d + 4; break;
      }
  
      h /= 6;
    }
  
    return [ h, s, v ];
  }