// Your web app's Firebase configuration
 var firebaseConfig = {
    apiKey: "AIzaSyB32mr_h-RE23Y5lmk232CK-MmzvkNc9i0",
    authDomain: "vishnu-6c873.firebaseapp.com",
    databaseURL: "https://vishnu-6c873-default-rtdb.firebaseio.com",
    projectId: "vishnu-6c873",
    storageBucket: "vishnu-6c873.appspot.com",
    messagingSenderId: "136853272936",
    appId: "1:136853272936:web:4428c3d4fc4e3a7ce93edd",
    measurementId: "G-HEHY8E6ZBM"
  };
// Initialize Firebase
firebase.initializeApp(firebaseConfig);

// Refernece contactInfo collections
let contactInfo = firebase.database().ref('user/'+name);

// Listen for a submit
document.querySelector(".contact-form").addEventListener("submit", submitForm);

function submitForm(e) {
  e.preventDefault();

  //   Get input Values
  let name = document.querySelector(".name").value;
  let bp = document.querySelector(".bp").value;
  let heartrate = document.querySelector(".heartrate").value;
  let sugarlevel = document.querySelector(".sugarlevel").value;
  let email = document.querySelector(".email").value;
  let message = document.querySelector(".message").value;
  console.log(name, bp, heartrate, sugarlevel, email, message);

  saveContactInfo(name, bp, heartrate, sugarlevel, email, message);

  document.querySelector(".contact-form").reset();
}

// Save infos to Firebase
function saveContactInfo(name, bp, heartrate, sugarlevel, email, message) {
  let newContactInfo = contactInfo.push();

  firebase.database().ref('user/'+name).set({
    name: name,
	bp: bp,
	heartrate: heartrate,
	sugarlevel: sugarlevel,
    email: email,
    message: message,
  });
}
