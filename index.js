const express = require('express')
const app = express()

const jsonld = require('jsonld')
const jsig = require('jsonld-signatures')
jsig.use('jsonld', jsonld)


app.get('/', (req, res) => {
  console.log(req.query)
  res.send('hello world')
})

const port = 3000;
app.listen(port, () => {
  console.log('Example app listening on port http://localhost:' + port);
});
