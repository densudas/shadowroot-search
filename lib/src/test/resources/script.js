(function () {
  let root = document.querySelector("#shadow-dom-container").attachShadow({mode: "open"});
  let h1 = document.createElement("h1");
  h1.textContent = "Inside Shadow DOM #1";
  h1.className = "inside";
  root.appendChild(h1);

  let div = document.createElement("div");
  h1.appendChild(div);
  root = div.attachShadow({mode: "open"});

  let div2 = document.createElement("div");
  root = root.appendChild(div2);

  let h2 = document.createElement("h2");
  h2.textContent = "Inside Shadow DOM #2";
  h2.className = "inside";
  root.appendChild(h2);
})()
