let prevGameData = null;
let playerPane = null;

const getGameJSON = () => {
	const Http = new XMLHttpRequest();
	const url = `http://${location.host}/getGameJSON`;
	Http.open("GET", url);
	Http.send();

	Http.onreadystatechange = (e) => {
	    if(Http.readyState === 4) {
	        //console.log('HTTP response text: ' + Http.responseText);
        	loadPage(JSON.parse(Http.responseText));
	    }
	};
};

const loadPage = (gameData) => {
	// Make an HTTP request to the server to receive the current game state
	//const gameData = getGameJSON();

	// gameData = JSON.parse(
	// 	'{"game":{"id":"758439574802","players":["Mario","Tobi","David","Felix","Eva","Niclas","Leander"],"plays":[{"type":"legislative-session","num":1,"president":"David","chancellor":"Tobi","rejected":false,"president_claim":"RRB","chancellor_claim":"RB","policy_played":"B","veto":false},{"type":"legislative-session","num":2,"president":"Felix","chancellor":"David","rejected":false,"president_claim":"RBB","chancellor_claim":"RR","policy_played":"R","veto":false},{"type":"legislative-session","num":3,"president":"Felix","chancellor":"David","rejected":true},{"type":"legislative-session","num":4,"president":"Felix","chancellor":"David","rejected":false,"president_claim":"RBB","chancellor_claim":"RR","policy_played":"R","veto":true},{"type":"executive-action","executive_action_type":"execution","president":"Felix","target":"David"},{"type":"executive-action","executive_action_type":"investigate_loyalty","president":"Felix","target":"David","claim":"B"},{"type":"executive-action","executive_action_type":"policy_peek","president":"Felix","claim":"RRR"},{"type":"executive-action","executive_action_type":"special_election","president":"Felix","target":"David"},{"type":"shuffle","fascist_policies":11,"liberal_policies":6}]}}'
	// );

	// gameData = JSON.parse(getGameJSON());

	console.log(gameData);

	let prevPlays = prevGameData == null ? null : prevGameData.game.plays;
	let plays = gameData.game.plays;

	//console.log(prevPlays);
	//console.log(plays);

	// console.log(prevPlays != null ? prevPlays.length - 1 : 0);

	//console.log(prevGameData);

	if (playerPane === null) {
		//console.log("PlayerPane");
		playerPane = new PlayerPane(gameData.game.players);
	}

	for (
		let i = prevPlays != null ? prevPlays.length  : 0;
		i < plays.length;
		i++
	) {
		let play = plays[i];
		//console.log(play.type);
		if (play.type === "legislative-session") {
			addLegislativeSession(play);
		} else if (play.type === "executive-action") {
			addExecutiveAction(play);
		} else if (play.type === "shuffle") {
			addShuffle(play);
		}
	}

	prevGameData = gameData;
};

const addLegislativeSession = (play) => {
	// Create the main div that contains all of the legislative session html
	let lsDiv = $(document.createElement("div"));
	lsDiv.addClass("game-action legislative-session");

	//Add the player names as clases, so that they can be recognised and made invisible/visible when the user clicks on the player in the player pane
	lsDiv.addClass(`player_${play.president}`);
	lsDiv.addClass(`player_${play.chancellor}`);

	// Create the main title of the legislative session div
	let lsTitle = $(document.createElement("h1"));
	lsTitle.addClass("game-action-title");
	// The text of the legislative session title contains special text if the chancellor was rejected or the policy vetoed
	lsTitle.text(
		`Legislative Session #${play.num} ${
		play.rejected === true ? "- Rejected" : ""
		} ${play.veto === true ? "- Vetoed" : ""}`
	);
	// Append the title to the main div
	lsDiv.append(lsTitle);

	// Append the president div and the chancellor div to the main div of the legislative session
	lsDiv.append(getLeaderDiv(play, "president"));
	lsDiv.append(getLeaderDiv(play, "chancellor"));

	// Create the div containing the data on the policy played/that would have been played (in case of veto)
	if (play.rejected === false) {
		let polPlayedDiv = $(document.createElement("div"));
		polPlayedDiv.addClass("policyplayed");

		let polPlayedSpan = $(document.createElement("span"));
		polPlayedSpan.addClass("policy-played-span");
		if (play.veto === true) polPlayedSpan.addClass("cross-out");
		polPlayedSpan.text("Policy played");

		polPlayedDiv.append(polPlayedSpan);

		let polPlayedIcon = $(document.createElement("img"));
		polPlayedIcon.addClass("policy-icon");
		polPlayedIcon.attr(
			"src",
			play.policy_played === "B" ? images.bird : images.skull
		);
		polPlayedIcon.attr("alt", "policy-played-icon");

		polPlayedDiv.append(polPlayedIcon);

		lsDiv.append(polPlayedDiv);
	}

	$("#game-log").append(lsDiv);
	//console.log(lsDiv.html());
};

const getLeaderDiv = (play, type) => {
	// Create the div containing the token for the leader and the name of the leader
	let leaderDiv = $(document.createElement("div"));
	leaderDiv.addClass(type);

	// Create the leader's token
	let leaderToken = $(document.createElement("img"));
	leaderToken.addClass("token");
	leaderToken.attr("src", images[`${type}_token`]);
	leaderToken.attr("alt", `${type}-token`);
	leaderDiv.append(leaderToken);

	// Create the leader's name span
	let leaderNameSpan = $(document.createElement("span"));
	leaderNameSpan.addClass(`${type}-name`);
	if (type === "chancellor" && play.rejected === true)
		leaderNameSpan.addClass("cross-out");
	leaderNameSpan.text(play[type]);
	leaderDiv.append(leaderNameSpan);

	// Create the leader's claim span
	if (play.rejected === false) {
		let leaderClaimSpan = $(document.createElement("span"));
		leaderClaimSpan.addClass("right-align");
		leaderClaimSpan.html(getColouredClaim(play[`${type}_claim`]));
		leaderDiv.append(leaderClaimSpan);
	}

	return leaderDiv;
};

const addExecutiveAction = (play) => {
	let executiveActionHTML = "";

	// Get HTML code that highlights the players names using the getPlayerHTML function
	let presidentHTML = getPlayerHTML(play.president);
	let targetHTML = play.target != undefined ? getPlayerHTML(play.target) : "";

	switch (play.executive_action_type) {
		case "execution":
			executiveActionHTML = `President ${presidentHTML} selects to execute ${targetHTML}.`;
			playerPane.displayDead(play.target);
			break;
		case "investigate_loyalty":
			executiveActionHTML = `President ${presidentHTML} sees the party membership of ${targetHTML} and claims to see a member of the ${
				play.claim === "B" ? "liberal" : "fascist"
				} team.`;
			playerPane.displayAccusation(play.target, play.claim);
			break;
		case "policy_peek":
			executiveActionHTML = `President ${presidentHTML} peeks at the next three policies and claims to see ${getColouredClaim(
				play.claim
			)}.`;
			break;
		case "special_election":
			executiveActionHTML = `President ${presidentHTML} has chosen to special elect ${targetHTML} as president.`;
			break;
		default:
			executiveActionHTML = "Unknwon executive action type";
			break;
	}

	// Create the main div containing all the other elements
	let execActDiv = $(document.createElement("div"));
	execActDiv.addClass("game-action executive-action");

	//Add the names of the involved players as clases, so that they can be recognised and made invisible/visible when the user clicks on the player in the player pane
	execActDiv.addClass(`player_${play.president}`);

	if (targetHTML != "") execActDiv.addClass(`player_${play.target}`);

	// Create the div containing the title and the executive action icon
	let execActDivTop = $(document.createElement("div"));
	execActDivTop.addClass("executive-action-top");

	execActDiv.append(execActDivTop);

	let execActTitle = $(document.createElement("h1"));
	execActTitle.addClass("game-action-title executive-action-title");
	execActTitle.text("Executive Action");

	execActDivTop.append(execActTitle);

	let execActImg = $(document.createElement("img"));
	execActImg.addClass("executive-action-icon");
	execActImg.attr("src", images[play.executive_action_type]);
	execActImg.attr("alt", `${play.executive_action_type}-icon`);

	execActDivTop.append(execActImg);

	// Create the p element that contains the text of the executive action
	let execActText = $(document.createElement("p"));
	execActText.addClass("executive-action-text");
	execActText.html(executiveActionHTML);

	execActDiv.append(execActText);

	$("#game-log").append(execActDiv);
};

// Function that returns HTML code that highlights the name of players
const getPlayerHTML = (name) => {
	return `<span class="player-name">${name != null ? name : ""}</span>`;
};

// Function that returns HTML code that correctly colours the letters in policy claims (liberals => blue, fascists => red)
const getColouredClaim = (claim) => {
	//console.log(claim.split(''));
	let result = "";
	let chars = claim.split("");

	chars.forEach((char) => {
		result += `<span class="${
			char === "R" ? "claim-span-red" : "claim-span-blue"
			}">${char}</span>`;
	});

	return result;
};

const addShuffle = (play) => {
	// Create the main container
	let shuffleDiv = $(document.createElement("div"));
	shuffleDiv.addClass("game-action shuffle-div");

	// Create the title of the shuffle div
	let shuffleDivTitle = $(document.createElement("h1"));
	shuffleDivTitle.addClass("game-action-title shuffle-title");
	shuffleDivTitle.text("Cards shuffled");

	shuffleDiv.append(shuffleDivTitle);

	// Create the container holding the number of policies and the image for both policy types
	let shuffleDivBody = $(document.createElement("div"));
	shuffleDivBody.addClass("shuffle-div-body");
	shuffleDiv.append(shuffleDivBody);

	shuffleDivBody.append(createPolicyCard(play, "fascist"));
	shuffleDivBody.append(createPolicyCard(play, "liberal"));

	// Append the shuffle div to the game-log section
	$("#game-log").append(shuffleDiv);
};

const createPolicyCard = (play, type) => {
	// Create the div for the policy card
	let policyDiv = $(document.createElement("div"));
	policyDiv.addClass("policy-number-div");

	// Create the image for the policy
	let policyImg = $(document.createElement("img"));
	policyImg.addClass("pol-card");
	policyImg.attr("src", images[`${type}_policy`]);
	policyImg.attr("alt", `${type}_policy_card`);

	policyDiv.append(policyImg);

	// Create the div showing the number of policies remaining in the draw pile
	let policyNum = $(document.createElement("div"));
	policyNum.addClass("policy-number");
	policyNum.text(play[`${type}_policies`]);

	policyDiv.append(policyNum);

	return policyDiv;
};

getGameJSON();

setInterval(getGameJSON, 2000);