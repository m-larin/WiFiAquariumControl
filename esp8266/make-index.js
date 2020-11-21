
const fs = require("fs");
const crypto = require("crypto");

const files = fs.readdirSync(".").reduce((p, c) => {
    if (fs.statSync(c).isFile()) {
		if (c != "index.json" && c != "make-index.js" && c != "make-index.cmd" && c != "settings.json")
        p.push(c);
    }
    return p;
}, []);


fs.unlink("index.json", (err) => {
  if (err) throw err;
  console.log('index.json was deleted');
});
fs.appendFileSync("index.json", "{\n");
(function makeHash(i) {
	
    const fd = fs.createReadStream(files[i]);
    const hash = crypto.createHash("sha1");
    hash.setEncoding("hex");
    fd.on("end", () => {
        hash.end();
        fs.appendFileSync("index.json", `\t"${files[i++]}": "${hash.read()}"`);
        if (i < files.length) {
			fs.appendFileSync("index.json", `,\n`);
            makeHash(i);
        }else{
			fs.appendFileSync("index.json", "\n}");			
			console.log('index.json was created');
		}
    });
    fd.pipe(hash);
})(0);

