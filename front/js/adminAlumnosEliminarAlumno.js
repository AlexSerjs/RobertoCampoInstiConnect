//adminAlumnosEliminarAlumno.js

document.addEventListener("DOMContentLoaded", function () {
    // Botón para cerrar el modal de eliminar alumnos
    const closeDeleteModalButton = document.querySelector(".close-delete-modal-btn");
    if (closeDeleteModalButton) {
      closeDeleteModalButton.addEventListener("click", function () {
        document.getElementById("delete-alumno-modal").classList.add("hidden");
        //console.log("Modal de eliminar cerrado");
      });
    } else {
      //console.error("El botón para cerrar el modal no se encontró en el DOM.");
    }

    const deleteAlumnoButton = document.getElementById("delete-alumno-btn");
  if (deleteAlumnoButton) {
    deleteAlumnoButton.addEventListener("click", deleteAlumno);
  //  console.log("Evento asociado al botón de eliminar alumno");
  } else {
//console.error("No se encontró el botón de eliminar alumno en el DOM.");
  }
  
    setupModalControls();
  });
  
  // Configurar controles del modal
  function setupModalControls() {
    // Botón para abrir el modal
    const openViewModalButton = document.querySelector(".open-view-modal-btn");
    if (openViewModalButton) {
      openViewModalButton.addEventListener("click", async () => {
        await populateAlumnosList();
        openModal("view-alumno-modal");
      });
    }
  
    // Overlay para cerrar el modal
    const modalOverlay = document.querySelector(".modal-overlay");
    if (modalOverlay) {
      modalOverlay.addEventListener("click", () => {
        document.getElementById("view-alumno-modal").classList.add("hidden");
      });
    }
  }
  
  // Función para abrir el modal
  function openModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.remove("hidden");
    }
  }
  
  // Función para cerrar el modal
  function closeModal(modalId) {
    const modal = document.getElementById(modalId);
    if (modal) {
      modal.classList.add("hidden");
    }
  }
  
  // Función para cargar la lista de alumnos
  async function populateDeleteAlumnosList() {
    try {
      const response = await fetch("http://localhost:8080/api/admin/alumnos");
     // console.log("Respuesta del servidor:", response);
  
      if (!response.ok) throw new Error("Error al obtener la lista de alumnos.");
  
      const alumnos = await response.json();
    //  console.log("Alumnos obtenidos:", alumnos);
  
      const deleteSelect = document.getElementById("alumnos-list-delete");
      deleteSelect.innerHTML = '<option value="">Seleccione un alumno</option>';
  
      alumnos.forEach((alumno) => {
        const option = document.createElement("option");
        option.value = alumno.id;
        option.textContent = alumno.nombreCompleto;
        deleteSelect.appendChild(option);
      });
    } catch (error) {
    //  console.error("Error en populateDeleteAlumnosList:", error);
     // alert("Hubo un problema al cargar la lista de alumnos.");
    }
  }
  
  // Función para abrir el modal de eliminar alumnos
  function openDeleteAlumnoModal() {
    populateDeleteAlumnosList(); // Cargar la lista de alumnos en el select
    openModal("delete-alumno-modal"); // Mostrar el modal
  }
  
  async function deleteAlumno() {
    const select = document.getElementById("alumnos-list-delete");
    const id = select.value;
  
   // console.log("ID seleccionado para eliminar:", id);
  
    if (!id) {
      alert("Por favor, selecciona un alumno.");
      return;
    }
  
    const confirmation = confirm("¿Estás seguro de que deseas eliminar este alumno?");
    if (!confirmation) {
    //  console.log("Eliminación cancelada por el usuario.");
      return;
    }
  
    try {
   //   console.log("Iniciando solicitud DELETE al backend...");
      const response = await fetch(`http://localhost:8080/api/admin/alumnos/${id}`, {
        method: "DELETE",
      });
  
   //   console.log("Respuesta del servidor:", response);
  
      if (!response.ok) {
        const result = await response.json();
     //   console.error("Error al eliminar el alumno:", result.error);
        alert(result.error || "Hubo un problema al eliminar el alumno.");
        return;
      }
  
      const result = await response.json();
     // console.log("Resultado del servidor:", result);
  
      if (result.isDelegado) {
        alert("El delegado y su grupo han sido eliminados exitosamente.");
      } else {
        alert("Alumno eliminado exitosamente.");
      }
  
      document.getElementById("delete-alumno-modal").classList.add("hidden");
      populateDeleteAlumnosList(); // Actualizar la lista después de la eliminación
    } catch (error) {
     // console.error("Error en la solicitud DELETE:", error);
      alert("Hubo un problema al eliminar el alumno.");
    }
  }
  
  
  