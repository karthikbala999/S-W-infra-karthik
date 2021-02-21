function getdata() {
    var user=document.getElementById("user").value;
    //firebase data retrieval function
    //path of your data
    //.once will get all your data in one time
    firebase.database().ref('user/'+user).once('value').then(function (snapshot) {
        //here we will get data
        //enter your field name
        var bp=snapshot.val().bp;
        var email=snapshot.val().email;
        var heartrate=snapshot.val().heartrate;
		var message=snapshot.val().message;
		var name=snapshot.val().name;
		var sugarlevel=snapshot.val().sugarlevel;

        //now we have data in variables
        //now show them in our html

        document.getElementById("bp").innerHTML=bp;
        document.getElementById("email").innerHTML=email;
        document.getElementById("heartrate").innerHTML=heartrate;
		document.getElementById("message").innerHTML=message;
		document.getElementById("name").innerHTML=name;
		document.getElementById("sugarlevel").innerHTML=sugarlevel;
    })
}