

Parse.Cloud.job("rankingAlgo", function(request, response) {
  var query = new Parse.Query("VidTrain");
  query.descending("createdAt");
  query.limit(1000);
  query.find({
    success: function(results) {
      for (var i = 0; i < results.length; ++i) {
        var object = results[i];
		var likeCount = object.get("likeCount")
		var createdAt = object.createdAt
		var seconds_since_founding =  Math.abs((object.createdAt.getTime()/1000 - 1134028003))
		var order = Math.log(Math.max(Math.abs(likeCount), 1))/ Math.LN10;

		var sign = 0
		if (likeCount > 0) {
		sign = 1
		} else if (likeCount < 0) {
		sign = -1
		}

		var finalRedditValue = (sign * order + seconds_since_founding / 45000)
		object.set("rankingValue", finalRedditValue)
		object.save()

      }
      response.success(results.length + " vidtrains");
    },
    error: function() {
      response.error("vidtrain lookup failed");
    }
  });
});