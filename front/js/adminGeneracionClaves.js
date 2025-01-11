document.addEventListener("DOMContentLoaded", async function () {
  const token = localStorage.getItem("token");

  // Configura la conexión del WebSocket
  const socket = new WebSocket(`ws://localhost:8080/ws/claves?token=${encodeURIComponent(token)}`);

  // Configura los eventos del WebSocket
  socket.onopen = () => {
     // console.log("Conexión WebSocket establecida.");
  };

  socket.onmessage = (event) => {
    //  console.log("Mensaje recibido del WebSocket:", event.data);
    //  alert("Nuevas claves generadas. Actualizando...");
      fetchTodasLasClaves(); // Llama al método para actualizar la tabla
  };

  socket.onerror = (error) => {
   //   console.error("Error en el WebSocket:", error);
  };

  socket.onclose = () => {
    //  console.log("Conexión WebSocket cerrada.");
  };

  

  if (!token) {
      window.location.href = "indexFct.html"; // Redirigir al login si no hay token
      return;
  }

  const abrirFormularioBtn = document.getElementById("abrirFormulario");
  const formulario = document.getElementById("generarClaves-form");
  const comunidadesDropdown = document.getElementById("comunidadesDropdown");
  const institutosDropdown = document.getElementById("institutosDropdown");
  const resultadoDiv = document.getElementById("resultado");
  const verClavesBtn = document.getElementById("verClaves");
  const resultadoClavesDiv = document.getElementById("resultado-claves");
  const clavesTableBody = document.getElementById("claves-table-body");
  const cerrarResultadoClavesBtn = document.getElementById("cerrar-resultado-claves-btn");

  abrirFormularioBtn.addEventListener("click", () => {
      formulario.style.display = "block";
      fetchComunidades(); // Cargar las comunidades al abrir el formulario
  });

  verClavesBtn.addEventListener("click", fetchTodasLasClaves); // Ver todas las claves al hacer clic
  cerrarResultadoClavesBtn.addEventListener("click", cerrarTodo); // Cerrar todo (claves generadas y tabla)

  // Obtener comunidades autónomas
  async function fetchComunidades() {
      try {
          const response = await fetch("http://localhost:8080/api/admin/instituto/comunidades", {
              headers: { Authorization: `Bearer ${token}` },
          });

          if (response.status === 401) {
              alert("Sesión expirada. Inicia sesión nuevamente.");
              window.location.href = "indexFct.html";
              return;
          }

          if (!response.ok) throw new Error("Error al obtener comunidades");

          const comunidades = await response.json();
          comunidadesDropdown.innerHTML = '<option value="">Seleccione una comunidad</option>';
          comunidades.forEach((comunidad) => {
              const option = document.createElement("option");
              option.value = comunidad.id;
              option.textContent = comunidad.nombre;
              comunidadesDropdown.appendChild(option);
          });

          comunidadesDropdown.addEventListener("change", fetchInstitutos);
      } catch (error) {
          console.error("Error al obtener comunidades:", error);
          comunidadesDropdown.innerHTML =
              '<option value="">Error al cargar comunidades</option>';
      }
  }

  // Obtener institutos por comunidad
  async function fetchInstitutos() {
      const comunidadId = comunidadesDropdown.value;
      if (!comunidadId) return;

      institutosDropdown.innerHTML = '<option value="">Cargando institutos...</option>';
      try {
          const response = await fetch(
              `http://localhost:8080/api/admin/instituto/comunidades/${comunidadId}/institutos`,
              { headers: { Authorization: `Bearer ${token}` } }
          );

          if (response.status === 401) {
              alert("Sesión expirada. Inicia sesión nuevamente.");
              window.location.href = "indexFct.html";
              return;
          }

          if (!response.ok) throw new Error("Error al obtener institutos");

          const institutos = await response.json();
          institutosDropdown.innerHTML = '<option value="">Seleccione un instituto</option>';
          institutos.forEach((instituto) => {
              const option = document.createElement("option");
              option.value = instituto.id;
              option.textContent = instituto.nombre;
              institutosDropdown.appendChild(option);
          });
      } catch (error) {
          console.error("Error al obtener institutos:", error);
          institutosDropdown.innerHTML =
              '<option value="">Error al cargar institutos</option>';
      }
  }

  // Generar claves
  formulario.addEventListener("submit", async function (event) {
      event.preventDefault();

      const comunidadId = comunidadesDropdown.value;
      const institutoId = institutosDropdown.value;
      const cantidad = document.getElementById("cantidad").value;

      if (!comunidadId || !institutoId || !cantidad) {
          alert("Por favor, complete todos los campos.");
          return;
      }

      try {
          const response = await fetch("http://localhost:8080/api/admin/claves/generar", {
              method: "POST",
              headers: {
                  "Content-Type": "application/json",
                  Authorization: `Bearer ${token}`,
              },
              body: JSON.stringify({
                  comunidadId: parseInt(comunidadId),
                  institutoId: parseInt(institutoId),
                  cantidad: parseInt(cantidad),
              }),
          });

          if (response.status === 401) {
              alert("Sesión expirada. Inicia sesión nuevamente.");
              window.location.href = "indexFct.html";
              return;
          }

          if (!response.ok) throw new Error("Error al generar claves");

          const data = await response.json();

          // Mostrar claves generadas inmediatamente
          resultadoDiv.innerHTML = `<h3>Claves Generadas:</h3><ul>${data.claves
              .map((clave) => `<li>${clave}</li>`)
              .join("")}</ul>`;

          // Restablecer campos
          comunidadesDropdown.value = "";
          institutosDropdown.value = "";
          institutosDropdown.innerHTML = '<option value="">Seleccione un instituto</option>';
          document.getElementById("cantidad").value = "";

          // Mostrar el botón cerrar y resultado
          resultadoClavesDiv.style.display = "block";
          cerrarResultadoClavesBtn.style.display = "inline-block";

      } catch (error) {
          console.error("Error al generar claves:", error);
          resultadoDiv.innerHTML = "<p>Error al generar claves. Por favor, intente nuevamente.</p>";
      }
  });

  // Obtener todas las claves
  async function fetchTodasLasClaves() {
      try {
          const response = await fetch("http://localhost:8080/api/admin/claves/todas", {
              headers: { Authorization: `Bearer ${token}` },
          });

          if (response.status === 401) {
              alert("Sesión expirada. Inicia sesión nuevamente.");
              window.location.href = "indexFct.html";
              return;
          }

          if (!response.ok) throw new Error("Error al obtener claves");

          const claves = await response.json();
          clavesTableBody.innerHTML = ""; // Limpiar la tabla

          claves.forEach((clave) => {
              const row = document.createElement("tr");
              row.innerHTML = `
                  <td>${clave.id}</td>
                  <td>${clave.clave}</td>
                  <td>${clave.comunidadNombre}</td>
                  <td>${clave.institutoNombre}</td>
                  <td>${clave.estado ? "Activa" : "Inactiva"}</td>
                  <td>${clave.fechaCreacion}</td>
                  <td>${clave.fechaUtilizacion || "N/A"}</td>
              `;
              clavesTableBody.appendChild(row);
          });

          resultadoClavesDiv.style.display = "block";
          cerrarResultadoClavesBtn.style.display = "inline-block";
      } catch (error) {
          console.error("Error al obtener claves:", error);
      }
  }

  // Función para cerrar resultados y formulario
  function cerrarTodo() {
      resultadoClavesDiv.style.display = "none"; // Ocultar tabla de claves
      formulario.style.display = "none"; // Ocultar formulario
      cerrarResultadoClavesBtn.style.display = "none"; // Ocultar botón cerrar
      resultadoDiv.innerHTML = ""; // Limpiar mensajes previos
  }

  function connectWebSocket() {
    const socket = new WebSocket(`ws://localhost:8080/ws/claves?token=${encodeURIComponent(token)}`);

    socket.onopen = () => {
        console.log("Conexión WebSocket establecida.");
    };

    socket.onmessage = (event) => {
        console.log("Mensaje recibido del WebSocket:", event.data);
        fetchTodasLasClaves(); // Actualiza la tabla
    };

    socket.onerror = (error) => {
        console.error("Error en el WebSocket:", error);
    };

    socket.onclose = (event) => {
        console.warn("Conexión WebSocket cerrada. Reconectando...");
        setTimeout(connectWebSocket, 5000); // Intenta reconectar en 5 segundos
    };
}

// Llama a la función para conectar el WebSocket
connectWebSocket();


});
