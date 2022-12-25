const express = require("express");
const app = express();
const bodyParser = require("body-parser");
var admin = require("firebase-admin");

app.use(bodyParser.json()); // to support JSON-encoded bodies
app.use(
    bodyParser.urlencoded({
        // to support URL-encoded bodies
        extended: true
    })
);

// Define the database: 
// Create mongodb database connection: 
// const dbURI = "mongodb://127.0.0.1/automatic-water-tank";

// Establish connection to database:
// mongoose
//     .connect(dbURI, {
//         useNewUrlParser: true,
//         useCreateIndex: true,
//         useUnifiedTopology: true,
//         useFindAndModify: true
//     })
//     .then(() => console.log("MongoDB connection successful"))
//     .catch(err => console.log(`MongoDB connection failed. Error: ${err}`));


// Define the REST API routes:
app.use("/api", require("./routes/index.js"));
app.use("/api/users", require("./routes/users"));

// Serve static assets in production: 
// if (process.env.NODE_ENV === 'production') {

//     app.use(express.static('client/build'));

//     app.get('*', (req, res) => {
//         res.sendFile(path.resolve(__dirname, 'client', 'build', 'index.html'));
//     });
// }


const PORT = process.env.PORT || 3000;

app.listen(PORT, () => console.log(`Automatic Water Tank server app listening on port ${PORT}!`));