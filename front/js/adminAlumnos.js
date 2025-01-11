document.addEventListener("DOMContentLoaded", function () {
  const closeViewModalButton = document.querySelector(".close-view-modal-btn");

  if (closeViewModalButton) {
  //  console.log("Botón para cerrar modal de ver detalles encontrado.");
    closeViewModalButton.addEventListener("click", () => {
  //    console.log("Clic en botón de cerrar modal de ver detalles.");
      closeModal("view-alumno-modal");
    });
  } else {
  //  console.error("No se encontró el botón para cerrar modal de ver detalles.");
  }
});
document.addEventListener("click", (event) => {
  if (event.target.classList.contains("close-view-modal-btn")) {
  //  console.log("Clic en botón de cerrar modal de ver detalles (delegación).");
    document.getElementById("view-alumno-modal").classList.add("hidden");
  }
});



function setupModalControls() {
  const closeViewModalButton = document.querySelector(".close-view-modal-btn");
 // console.log("Botón encontrado:", closeViewModalButton); // Log para depuración

  if (closeViewModalButton) {
    closeViewModalButton.addEventListener("click", () => {
    //  console.log("Clic en botón de cerrar modal de ver detalles."); // Confirmación del evento
      document.getElementById("view-alumno-modal").classList.add("hidden");
    });
  } else {
  //  console.error("No se encontró el botón para cerrar modal de ver detalles.");
  }


 // Overlay para cerrar el modal de ver detalles
 const viewOverlay = document.querySelector(".view-overlay");
 if (viewOverlay) {
   viewOverlay.addEventListener("click", () => {
   //  console.log("Clic en el overlay para cerrar modal de ver detalles.");
     document.getElementById("view-alumno-modal").classList.add("hidden");
   });
 }
}

// Función para mostrar el modal
function openModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.remove("hidden");
    //console.log(Modal con ID: ${modalId} abierto.);
  } else {
   // console.error(No se encontró el modal con ID: ${modalId}.);
  }
}

// Función para cerrar el modal
function closeModal(modalId) {
  const modal = document.getElementById(modalId);
  if (modal) {
    modal.classList.add("hidden");
   // console.log(Modal con ID: ${modalId} cerrado.);
  } else {
    //console.error(No se encontró el modal con ID: ${modalId}.);
  }
}

// Función para cargar la lista de alumnos
async function populateAlumnosList() {
  try {
    const response = await fetch("http://localhost:8080/api/admin/alumnos");
    if (!response.ok) throw new Error("Error al obtener la lista de alumnos.");

    const alumnos = await response.json();
    const viewSelect = document.getElementById("alumnos-list");

    viewSelect.innerHTML = '<option value="">Seleccione un alumno</option>';
    alumnos.forEach((alumno) => {
      const option = document.createElement("option");
      option.value = alumno.id;
      option.textContent = alumno.nombreCompleto;
      viewSelect.appendChild(option);
    });
  } catch (error) {
    alert("Hubo un problema al cargar la lista de alumnos.");
  }
}



// Función para obtener detalles de un alumno seleccionado
function fetchAlumnoDetails() {
  const select = document.getElementById("alumnos-list");
  const id = select.value;

  if (!id) {
    alert("Por favor, selecciona un alumno.");
    return;
  }

  fetch(`http://localhost:8080/api/admin/alumnos/detalles/${id}`)
    .then((response) => {
      if (!response.ok) {
        throw new Error("Error al obtener los detalles.");
      }
      return response.json();
    })
    .then((alumno) => {
      updateAlumnoDetailsUI(alumno);
      document.querySelector("#view-alumno-modal .modal-content").classList.add("show-data");
    })
    .catch((error) => {
     // console.error("Error al obtener detalles:", error);
      alert("Hubo un problema al obtener los detalles.");
    });
}

// Función para actualizar la interfaz con los detalles del alumno
function updateAlumnoDetailsUI(alumno) {
 // console.log("Actualizando detalles del alumno:", alumno);

  document.getElementById("alumno-id").textContent = alumno.id || "No disponible";
  document.getElementById("alumno-nombre").textContent = alumno.nombreCompleto || "No disponible";
  document.getElementById("alumno-codigo-grupo").textContent = alumno.codigoGrupo || "No disponible";
  document.getElementById("alumno-email").textContent = alumno.email || "No disponible";
  document.getElementById("alumno-tipo").textContent = alumno.tipo || "No disponible";
  document.getElementById("alumno-verificado").textContent = alumno.isVerified ? "Sí" : "No";
  document.getElementById("alumno-puede-publicar").textContent = alumno.puedePublicar ? "Sí" : "No";
  document.getElementById("alumno-fecha-creacion").textContent = alumno.fechaCreacion
    ? new Date(alumno.fechaCreacion).toLocaleString()
    : "No disponible";
  document.getElementById("alumno-ciclo").textContent = alumno.nombreCiclo || "No disponible";
  document.getElementById("alumno-nivel").textContent = alumno.nivelCiclo || "No disponible";
  document.getElementById("alumno-anio").textContent = alumno.anioCiclo || "No disponible";
  document.getElementById("alumno-anio-lectivo").textContent = alumno.anioLectivo || "No disponible";
  document.getElementById("alumno-instituto").textContent = alumno.instituto || "No disponible";
  document.getElementById("alumno-comunidad").textContent = alumno.comunidadAutonoma || "No disponible";
}
