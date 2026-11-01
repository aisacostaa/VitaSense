
(() => {
    "use strict";

    const STORAGE_KEY = "vitasense-menu-last-index";

    function iniciarMenuGlass() {
        const menu = document.querySelector(".menu");

        if (!menu || menu.dataset.glassInitialized === "true") {
            return;
        }

        const links = Array.from(menu.querySelectorAll("a"));

        if (!links.length) {
            return;
        }

        const ativo = links.findIndex(link => link.classList.contains("on"));

        if (ativo < 0) {
            return;
        }

        menu.dataset.glassInitialized = "true";

        const lens = document.createElement("glass-element");
        lens.className = "menu-glass-lens";
        lens.setAttribute("aria-hidden", "true");

        const content = document.createElement("div");
        content.className = "menu-glass-content";

        lens.appendChild(content);
        menu.appendChild(lens);

        function copiarConteudo(link) {
            content.replaceChildren();

            Array.from(link.children).forEach(element => {
                content.appendChild(element.cloneNode(true));
            });
        }

        function posicionar(link, animar = true) {
            if (!link) return;

            const menuRect = menu.getBoundingClientRect();
            const linkRect = link.getBoundingClientRect();

            const x = linkRect.left - menuRect.left + menu.scrollLeft;
            const y = linkRect.top - menuRect.top;

            if (!animar) {
                lens.style.transition = "none";
            }

            lens.style.width = `${linkRect.width}px`;
            lens.style.height = `${linkRect.height}px`;
            lens.style.transform = `translate3d(${x}px, ${y}px, 0)`;

            if (!animar) {
                requestAnimationFrame(() => {
                    lens.style.transition = "";
                });
            }
        }

        function atualizarConteudo(link) {
            copiarConteudo(link);
        }

        const indiceAnterior = Number(
            sessionStorage.getItem(STORAGE_KEY)
        );

        const temIndiceAnterior =
            Number.isInteger(indiceAnterior) &&
            indiceAnterior >= 0 &&
            indiceAnterior < links.length &&
            indiceAnterior !== ativo;

        if (temIndiceAnterior) {
            atualizarConteudo(links[indiceAnterior]);
            posicionar(links[indiceAnterior], false);
        } else {
            atualizarConteudo(links[ativo]);
            posicionar(links[ativo], false);
        }

        requestAnimationFrame(() => {
            lens.classList.add("is-visible");

            if (temIndiceAnterior) {
                requestAnimationFrame(() => {
                    posicionar(links[ativo], true);
                });
            }
        });

        links[ativo].classList.add("glass-active");

        lens.addEventListener("transitionend", event => {
            if (event.propertyName === "transform") {
                atualizarConteudo(links[ativo]);
            }
        });

        links.forEach((link, index) => {
            link.addEventListener("click", () => {
                sessionStorage.setItem(STORAGE_KEY, String(index));
            });
        });

        window.addEventListener("resize", () => {
            posicionar(links[ativo], false);
        });

        menu.addEventListener("scroll", () => {
            posicionar(links[ativo], false);
        }, { passive: true });

        if ("ResizeObserver" in window) {
            const observer = new ResizeObserver(() => {
                posicionar(links[ativo], false);
            });

            observer.observe(menu);
            links.forEach(link => observer.observe(link));
        }
    }

    if (document.readyState === "loading") {
        document.addEventListener("DOMContentLoaded", iniciarMenuGlass);
    } else {
        iniciarMenuGlass();
    }
})();
