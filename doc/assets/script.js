const $ = document.querySelector.bind(document)
const $$ = document.querySelectorAll.bind(document)

$$("[class^='section-number-']").forEach((section) => {
  const linkTarget = section.parentNode.id.match(/^org/) ? section.innerText : section.parentNode.id
  section.innerHTML = `<a href="#${linkTarget}">§<a name="${linkTarget}"></a></a>`
})
