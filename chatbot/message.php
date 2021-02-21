<!-- Created By CodingNepal -->
<?php
// connecting to database
$conn = mysqli_connect("localhost", "root","", "chatbot") or die("Database Error");

// getting user message through ajax
$getMesg = mysqli_real_escape_string($conn, $_POST['text']);

//checking user query to database query
$check_data = "SELECT * FROM t_chatbot WHERE name LIKE '%$getMesg%'";
$run_query = mysqli_query($conn, $check_data) or die("Error");

// if user query matched to database query we'll show the reply otherwise it go to else statement
if(mysqli_num_rows($run_query) > 0){
    //fetching replay from the database according to the user query
    $fetch_data = mysqli_fetch_assoc($run_query);
    //storing replay to a varible which we'll send to ajax
    echo nl2br("bp=");
		$replay1 = $fetch_data['bp'];
		echo $replay1;
	echo nl2br("\nsugarlevel=");
		$replay2 = $fetch_data['sugarlevel'];
		echo $replay2;
	echo nl2br("\nheartrate=");
		$replay3 = $fetch_data['heartrate'];
		echo $replay3;
	echo nl2br("\nmessage=");
		$replay4 = $fetch_data['message'];
		echo $replay4;
}else{
    echo "Sorry can't be able to understand you!";
}

?>
