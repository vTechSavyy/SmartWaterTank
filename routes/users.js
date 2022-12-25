const express = require("express");
const router = express.Router();

const PIN = "9142";

// LHTTP login request:
router.post("/login", async (req, res) => {


    const pin = req.body.pin;

    console.log(" PIN received is: ", pin);

    // Find the user name in the MDB database: 
    if (pin === PIN) {
        res.status(200).json({ msg: 'Login successful!' })
    }
    else {
        res.status(400).json({ msg: 'Invalid PIN. Please try again!' });
    }

    // 

});


module.exports = router;