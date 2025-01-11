document.addEventListener("DOMContentLoaded", () => {
    const explorarBtn = document.getElementById("explorar-institutos-btn");
    const cerrarGestionBtn = document.getElementById("cerrar-gestion-institutos-btn");
  
    const gestionSection = document.getElementById("gestion-institutos-dinamico");
    const comunidadesDropdown = document.getElementById("comunidades-dropdown");
    const institutosDropdown = document.getElementById("institutos-dropdown");
    const cursosTable = document.getElementById("cursos-table");
    const gruposTable = document.getElementById("grupos-table");
  
    const institutosContainer = document.getElementById("institutos-container");
    const cursosContainer = document.getElementById("cursos-container");
    const gruposContainer = document.getElementById("grupos-container");
  
    // Botón para explorar institutos
    explorarBtn.addEventListener("click", () => {
      gestionSection.classList.remove("hidden");
      comunidadesDropdown.innerHTML = '<option value="">Cargando comunidades...</option>';
      fetchComunidades();
    });
  
    // Botón para cerrar gestión
    cerrarGestionBtn.addEventListener("click", () => {
      gestionSection.classList.add("hidden");
      limpiarGestion();
    });
  
    // Fetch comunidades autónomas
    async function fetchComunidades() {
      try {
        const response = await fetch("http://localhost:8080/api/admin/instituto/comunidades");
        if (!response.ok) throw new Error(`Error al obtener comunidades: ${response.status} ${response.statusText}`);
  
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
        comunidadesDropdown.innerHTML = '<option value="">Error al cargar comunidades</option>';
      }
    }
  
    // Fetch institutos por comunidad
    async function fetchInstitutos() {
      const comunidadId = comunidadesDropdown.value;
      if (!comunidadId) return;
  
      institutosContainer.classList.remove("hidden");
      institutosDropdown.innerHTML = '<option value="">Cargando institutos...</option>';
      cursosContainer.classList.add("hidden");
      gruposContainer.classList.add("hidden");
  
      try {
        const response = await fetch(`http://localhost:8080/api/admin/instituto/comunidades/${comunidadId}/institutos`);
        if (!response.ok) throw new Error(`Error al obtener institutos: ${response.status} ${response.statusText}`);
  
        const institutos = await response.json();
        institutosDropdown.innerHTML = '<option value="">Seleccione un instituto</option>';
        institutos.forEach((instituto) => {
          const option = document.createElement("option");
          option.value = instituto.id;
          option.textContent = instituto.nombre;
          institutosDropdown.appendChild(option);
        });
  
        institutosDropdown.addEventListener("change", fetchCursosYGrupos);
      } catch (error) {
        console.error("Error al obtener institutos:", error);
        institutosDropdown.innerHTML = '<option value="">Error al cargar institutos</option>';
      }
    }
  
  
    // Fetch cursos y grupos por instituto
    async function fetchCursosYGrupos() {
      const institutoId = institutosDropdown.value;
      if (!institutoId) return;
  
      // Mostrar mensajes de "Cargando..." mientras se obtienen los datos
      cursosTable.innerHTML = '<tr><td colspan="3">Cargando cursos...</td></tr>';
      gruposTable.innerHTML = '<tr><td colspan="3">Cargando grupos...</td></tr>';
  
      try {
        // Fetch Cursos
        await fetchCursos(institutoId);
  
        // Fetch Grupos
        await fetchGrupos(institutoId);
      } catch (error) {
        console.error("Error al obtener cursos o grupos:", error);
      }
    }
  
    // Fetch cursos por instituto
    async function fetchCursos(institutoId) {
      try {
        const cursosResponse = await fetch(`http://localhost:8080/api/admin/instituto/institutos/${institutoId}/cursos`);
        if (!cursosResponse.ok) throw new Error(`Error al obtener cursos: ${cursosResponse.status} ${cursosResponse.statusText}`);
  
        const cursos = await cursosResponse.json();
        cursosContainer.classList.remove("hidden");
        cursosTable.innerHTML = `
          <tr>
            <th>Nombre</th>
            <th>Nivel</th>
            <th>Año</th>
          </tr>
        `;
  
        if (cursos.length === 0) {
          cursosTable.innerHTML += '<tr><td colspan="3">No se encontraron cursos para este instituto.</td></tr>';
        } else {
          cursos.forEach((curso) => {
            const row = document.createElement("tr");
            row.innerHTML = `
              <td>${curso.nombre}</td>
              <td>${curso.nivel}</td>
              <td>${curso.anio}</td>
            `;
            cursosTable.appendChild(row);
          });
        }
      } catch (error) {
        console.error("Error al obtener cursos:", error);
        cursosTable.innerHTML = '<tr><td colspan="3">Error al cargar cursos.</td></tr>';
      }
    }
  
    // Fetch grupos por instituto
    async function fetchGrupos(institutoId) {
      try {
        const gruposResponse = await fetch(`http://localhost:8080/api/admin/instituto/institutos/${institutoId}/grupos`);
        if (!gruposResponse.ok) throw new Error(`Error al obtener grupos: ${gruposResponse.status} ${gruposResponse.statusText}`);
  
        const grupos = await gruposResponse.json();
        gruposContainer.classList.remove("hidden");
        gruposTable.innerHTML = `
          <tr>
            <th>Código de Grupo</th>
            <th>Nombre del Curso</th>
            <th>Delegado</th>
          </tr>
        `;
  
        if (grupos.length === 0) {
          gruposTable.innerHTML += '<tr><td colspan="3">No se encontraron grupos para este instituto.</td></tr>';
        } else {
          grupos.forEach((grupo) => {
            const row = document.createElement("tr");
            row.innerHTML = `
              <td>${grupo.codigoGrupo}</td>
              <td>${grupo.nombreCurso}</td>
              <td>${grupo.nombreDelegado}</td>
            `;
            gruposTable.appendChild(row);
          });
        }
      } catch (error) {
        console.error("Error al obtener grupos:", error);
        gruposTable.innerHTML = '<tr><td colspan="3">Error al cargar grupos.</td></tr>';
      }
    }
  
    // Limpia la gestión
    function limpiarGestion() {
      comunidadesDropdown.innerHTML = '<option value="">Seleccione una comunidad</option>';
      institutosDropdown.innerHTML = '<option value="">Seleccione un instituto</option>';
      cursosTable.innerHTML = "";
      gruposTable.innerHTML = "";
  
      institutosContainer.classList.add("hidden");
      cursosContainer.classList.add("hidden");
      gruposContainer.classList.add("hidden");
    }
  });
  