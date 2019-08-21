const functions = require('firebase-functions');
const admin = require('firebase-admin');
const uuidv4 = require('uuid/v4');

admin.initializeApp(functions.config().firebase);

// // Create and Deploy Your First Cloud Functions
// // https://firebase.google.com/docs/functions/write-firebase-functions
//
exports.createUserDocumentWhenSignInAnonymously = functions.auth.user().onCreate((user) => {

  	
	let db = admin.firestore();
	let docRef = db.collection('users').doc().set({
			"uid": uuidv4(),
            "firebase_uid": user.uid,
            "firefox_uid": "",
            "created_timestamp": Date.now(),
            "updated_timestamp": Date.now(),
            "version": "1",
            "email": "",
            "status": "anonymous"
            
        });


})