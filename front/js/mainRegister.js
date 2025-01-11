document.addEventListener('DOMContentLoaded', function () {
    const participantBtn = document.getElementById('participantBtn');
    const delegateBtn = document.getElementById('delegateBtn');
    const participantForm = document.getElementById('participantForm');
    const delegateForm = document.getElementById('delegateForm');
    const regionSelect = document.getElementById('region');
    const instituteSelect = document.getElementById('instituteName');
    const cicloFormativoSelect = document.getElementById('cicloFormativo');
    const academicYearField = document.getElementById('academicYear');
    const emailDelegateField = document.getElementById('emailDelegate');
    let dominioCorreo = '';
    const defaultImage = 'images/iconDefault.jpg';
    const groupCodeInput = document.getElementById('groupCode'); 
    const emailInput = document.getElementById('email');



       
        const modalClave = document.getElementById('claveDelegadoModal');
        const validarClaveDelegadoBtn = document.getElementById('validarClaveDelegadoBtn');
        const cerrarClaveDelegadoModalBtn = document.getElementById('cerrarClaveDelegadoModalBtn');
        const claveDelegadoInput = document.getElementById('claveDelegadoInput');
        const claveDelegadoError = document.getElementById('claveDelegadoError');
    
        // Mostrar el formulario de Participante
        participantBtn.addEventListener('click', function () {
            showForm(participantForm, delegateForm);
        });
    
        // Mostrar el modal de validación al hacer clic en "Delegado"
        delegateBtn.addEventListener('click', function () {
            // Mostrar el modal
            modalClave.style.display = 'block';
            claveDelegadoInput.value = ''; // Limpiar el campo de entrada
            claveDelegadoError.style.display = 'none'; // Ocultar error
        });
    
        // Validar la clave
        validarClaveDelegadoBtn.addEventListener('click', async function () {
            const clave = claveDelegadoInput.value.trim();
    
            if (!clave) {
                claveDelegadoError.textContent = 'Por favor, ingrese una clave.';
                claveDelegadoError.style.display = 'block';
                return;
            }
    
            try {
                const response = await fetch(
                    `http://localhost:8080/api/admin/claves/verificar-clave?clave=${encodeURIComponent(clave)}`,
                    {
                        method: 'GET',
                        headers: { 'Content-Type': 'application/json' },
                    }
                );
    
                if (!response.ok) {
                    throw new Error('Error al validar la clave');
                }
    
                const data = await response.json();
    
                if (data.valida) {
                    modalClave.style.display = 'none'; // Cerrar modal
                    showForm(delegateForm, participantForm); // Mostrar formulario de delegado
                } else {
                    claveDelegadoError.textContent = 'Clave incorrecta. Intente nuevamente.';
                    claveDelegadoError.style.display = 'block';
                }
            } catch (error) {
                console.error('Error al validar la clave:', error);
                claveDelegadoError.textContent = 'Hubo un problema. Inténtelo más tarde.';
                claveDelegadoError.style.display = 'block';
            }
        });
    
        // Cerrar el modal al hacer clic en "Cerrar"
        cerrarClaveDelegadoModalBtn.addEventListener('click', function () {
            modalClave.style.display = 'none';
        });
    
        // Función para mostrar y limpiar el formulario seleccionado
        function showForm(formToShow, formToHide) {
            formToHide.style.display = 'none';
            formToShow.style.display = 'block';
    
            // Limpiar todos los campos del formulario ocultado
            Array.from(formToHide.elements).forEach(element => {
                if (element.type !== 'button' && element.type !== 'submit') {
                    element.value = '';
                }
            });
        }
  
    
  

    // Función para cargar las comunidades autónomas
    function cargarComunidades() {
        fetch('http://localhost:8080/api/comunidades')
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la solicitud');
                }
                console.log("Respuesta recibida:", response); // Log para verificar la respuesta
                return response.json();
            })
            .then(comunidades => {
                console.log("Datos de comunidades:", comunidades); // Log para ver los datos de las comunidades
                regionSelect.innerHTML = '<option value="">Seleccione su comunidad</option>';
                comunidades.forEach(comunidad => {
                    const option = document.createElement('option');
                    option.value = comunidad.nombre;
                    option.textContent = comunidad.nombre;
                    regionSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error al cargar las comunidades autónomas:', error);
            });
    }






 // Función para cargar los institutos según la comunidad autónoma seleccionada (ya definida anteriormente)
       function cargarInstitutos(region) {
        fetch(`http://localhost:8080/api/institutos?region=${encodeURIComponent(region)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la solicitud de institutos');
                }
                return response.json();
            })
            .then(institutos => {
                instituteSelect.innerHTML = '<option value="">Seleccione su instituto</option>';
                institutos.forEach(instituto => {
                    const option = document.createElement('option');
                    option.value = instituto.nombre;
                    option.textContent = instituto.nombre;
                    instituteSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error al cargar los institutos:', error);
            });
    }

    // Evento para detectar el cambio de comunidad autónoma y cargar los institutos correspondientes
    regionSelect.addEventListener('change', function () {
        const selectedRegion = regionSelect.value;
        if (selectedRegion) {
            cargarInstitutos(selectedRegion);
        } else {
            instituteSelect.innerHTML = '<option value="">Seleccione su instituto</option>';
        }
    });







    // Función para cargar los ciclos formativos según el instituto seleccionado
    function cargarCiclosFormativos(institutoNombre) {
        fetch(`http://localhost:8080/api/ciclos_formativos?institutoNombre=${encodeURIComponent(institutoNombre)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la solicitud de ciclos formativos');
                }
                return response.json();
            })
            .then(ciclosFormativos => {
                cicloFormativoSelect.innerHTML = '<option value="">Seleccione el Grado</option>';
                ciclosFormativos.forEach(ciclo => {
                    const option = document.createElement('option');
                    option.value = JSON.stringify({ nombre: ciclo.nombre, anio: ciclo.anio });// Puedes ajustar esto según el valor que desees enviar
                    option.textContent = `${ciclo.nombre} - ${ciclo.nivel} - ${ciclo.anio}`;
                    cicloFormativoSelect.appendChild(option);
                });
            })
            .catch(error => {
                console.error('Error al cargar los ciclos formativos:', error);
            });
    }

    // Evento para detectar el cambio de comunidad autónoma y cargar los institutos correspondientes
    regionSelect.addEventListener('change', function () {
        const selectedRegion = regionSelect.value;
        if (selectedRegion) {
            cargarInstitutos(selectedRegion);
            instituteSelect.innerHTML = '<option value="">Seleccione su instituto</option>'; // Limpiar ciclo formativo también
            cicloFormativoSelect.innerHTML = '<option value="">Seleccione el Grado</option>';
        } else {
            instituteSelect.innerHTML = '<option value="">Seleccione su instituto</option>';
            cicloFormativoSelect.innerHTML = '<option value="">Seleccione el Grado</option>';
        }
    });

    // Evento para detectar el cambio de instituto y cargar los ciclos formativos correspondientes
    instituteSelect.addEventListener('change', function () {
        const selectedInstitute = instituteSelect.value;
        if (selectedInstitute) {
            cargarCiclosFormativos(selectedInstitute);
        } else {
            cicloFormativoSelect.innerHTML = '<option value="">Seleccione el Grado</option>';
        }
    });




    // Función para el año lectivo 
    function setAcademicYear() {
        const today = new Date();
        const currentYear = today.getFullYear();
        const currentMonth = today.getMonth() + 1; 
       
        let academicYear;
        if (currentMonth >= 8) {
            academicYear = `${currentYear}-${currentYear + 1}`;
        } else {
            academicYear = `${currentYear - 1}-${currentYear}`;
        }

        academicYearField.value = academicYear;
    }
    setAcademicYear();


    


    // Función para cargar el dominio de correo basado en la comunidad autónoma seleccionada
    function cargarDominioCorreo(comunidad) {
        fetch(`http://localhost:8080/api/comunidades/dominio?comunidad_autonoma=${encodeURIComponent(comunidad)}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener el dominio de correo');
                }
                return response.json();
            })
            .then(data => {
                dominioCorreo = data.dominio_correo;
                emailDelegateField.placeholder = `nombre_usuario${dominioCorreo}`;
                emailDelegateField.value = ''; // Limpia el campo para que el usuario pueda empezar a escribir el nombre

                // Elimina el event listener previo (si existe) para evitar duplicados
                emailDelegateField.removeEventListener('input', actualizarCorreo);
                // Agrega un nuevo event listener que actualiza el valor con el dominio actual
                emailDelegateField.addEventListener('input', actualizarCorreo);
            })
            .catch(error => {
                console.error('Error al cargar el dominio de correo:', error);
            });
    }

    // Función para actualizar el correo electrónico con el dominio
    function actualizarCorreo() {
        const nombreUsuario = emailDelegateField.value.split('@')[0]; // Solo obtiene la parte del usuario
        // Comentamos la línea que bloquea el dominio automáticamente para permitir edición
        // emailDelegateField.value = `${nombreUsuario}${dominioCorreo}`; // Combina con el dominio
        
        // Permite que el usuario mantenga o edite el dominio manualmente
        if (!emailDelegateField.value.includes('@')) {
            emailDelegateField.value = `${nombreUsuario}${dominioCorreo}`; // Si no tiene dominio, agrega el predeterminado
        }
    }

    // Evento para detectar el cambio en la comunidad autónoma y cargar el dominio de correo correspondiente
    regionSelect.addEventListener('change', function () {
        const selectedRegion = regionSelect.value;
        if (selectedRegion) {
            cargarDominioCorreo(selectedRegion);
        } else {
            emailDelegateField.placeholder = "email"; // Restablece el placeholder si no se selecciona ninguna comunidad
            emailDelegateField.value = ""; // Limpia el valor si se cambia la comunidad
            dominioCorreo = ''; // Restablece el dominio de correo a vacío
        }
    });




    function handlePhotoInput(inputId, previewId) {
        const photoInput = document.getElementById(inputId);
        const previewImage = document.getElementById(previewId);

        // Oculta la imagen de vista previa inicialmente
        previewImage.style.display = 'none';

        // Muestra la imagen seleccionada o la predeterminada si no hay ninguna
        photoInput.addEventListener('change', function () {
            const file = photoInput.files[0];

            if (file) {
                const reader = new FileReader();
                reader.onload = function (e) {
                    previewImage.src = e.target.result; // Muestra la imagen seleccionada
                    previewImage.style.display = 'block'; // Muestra la imagen de vista previa
                };
                reader.readAsDataURL(file);
            } else {
                previewImage.src = defaultImage; // Muestra la imagen predeterminada si no se selecciona ninguna
                previewImage.style.display = 'none'; // Oculta la imagen de vista previa si no hay archivo
            }
        });
    }

    // Llamadas para los distintos campos de foto
    handlePhotoInput('photo', 'previewImagePhoto');
    handlePhotoInput('photoDelegate', 'previewImageDelegate');


    // ------------------------------------------------------------------------------------------------------------------------------------------------------------

    
    // Alumno form



    // Función para obtener el dominio de correo según el prefijo
    function obtenerDominioCorreo(prefijo) {
        fetch(`http://localhost:8080/api/comunidades/dominio-correo?codigo_prefijo=${prefijo}`)
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error al obtener el dominio de correo');
                }
                return response.json();
            })
            .then(data => {
                const dominioCorreo = data.dominio_correo;
                const nombreUsuario = emailInput.value.split('@')[0]; // Mantiene solo la parte del usuario
                emailInput.value = `${nombreUsuario}${dominioCorreo}`; // Actualiza el correo con el dominio
            })
            .catch(error => {
                console.error('Error al cargar el dominio de correo:', error);
            });
    }

    // Evento para detectar el cambio en el campo de código de grupo
    groupCodeInput.addEventListener('input', function () {
        const codigoGrupo = groupCodeInput.value;
        const prefijo = codigoGrupo.split('-')[0]; // Obtiene el prefijo antes del guion

        // Verifica si el prefijo es válido (2 o más caracteres)
        if (prefijo.length >= 2) {
            obtenerDominioCorreo(prefijo);
        } else {
            emailInput.placeholder = "email"; // Restablece el placeholder si el prefijo no es válido
            emailInput.value = ""; // Limpia el valor del campo de correo
        }
    });


    function setupTogglePassword(passwordFieldId) {
        const passwordField = document.getElementById(passwordFieldId);
        const togglePassword = passwordField.nextElementSibling;
        const eyeOpen = togglePassword.querySelector('#eyeOpen');
        const eyeClosed = togglePassword.querySelector('#eyeClosed');

        // Evento para mostrar/ocultar la contraseña
        togglePassword.addEventListener('click', function () {
            if (passwordField.type === "password") {
                passwordField.type = "text";
                eyeOpen.style.display = "none";
                eyeClosed.style.display = "inline";
            } else {
                passwordField.type = "password";
                eyeOpen.style.display = "inline";
                eyeClosed.style.display = "none";
            }
        });
    }

    // Configura el toggle para ambos campos de contraseña
    setupTogglePassword('password');
    setupTogglePassword('passwordDelegate');

   
    cargarComunidades();


    // ------------------------------------------------------------------------------------------------------------------------------------------------------------

    
    // Delegado submit

    delegateForm.addEventListener('submit', function (event) {
        event.preventDefault();
    
        // Mostrar el overlay de carga
        document.getElementById('loadingOverlay').style.display = 'flex';
    
        // Obtiene el ciclo formativo y el año seleccionado
        const cicloFormativoData = JSON.parse(document.getElementById('cicloFormativo').value || '{}');
        const cicloFormativoNombre = cicloFormativoData.nombre || ""; // Nombre del ciclo formativo
        const anio = cicloFormativoData.anio || ""; // Año del ciclo formativo (primero o segundo)
    
        // Captura los datos del formulario
        const data = {
            institutoNombre: document.getElementById('instituteName').value || "",
            cicloFormativo: cicloFormativoNombre,
            anioLectivo: document.getElementById('academicYear').value || "",
            anio: anio,
            nombreCompleto: document.getElementById('fullNameDelegate').value || "",
            foto: document.getElementById('photoDelegate').files[0] ? document.getElementById('photoDelegate').files[0].name : "iconDefault.jpg",
            correoEducativo: document.getElementById('emailDelegate').value || "",
            clave: document.getElementById('passwordDelegate').value || ""
        };
    
        // Realiza el fetch para enviar los datos
        fetch('http://localhost:8080/api/alumnos/delegado', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(data)
        })
        .then(response => {
            if (!response.ok) {
                throw new Error('Error en la solicitud de registro de delegado');
            }
            return response.json();
        })
        .then(result => {
            console.log('Registro exitoso:', result);
            
    
            // Si el servidor devuelve un token de verificación, almacenarlo en localStorage
            if (result.verificationToken) {
                localStorage.setItem('verificationToken', result.verificationToken);
            }
    
            // Oculta el overlay de carga
            document.getElementById('loadingOverlay').style.display = 'none';
    
            // Redirige a estadoRegistro.html con estado de éxito
            window.location.href = 'estadoRegistro.html?status=success';
        })
        .catch(error => {
            console.error('Error al registrar el delegado:', error);
    
            // Oculta el overlay de carga
            document.getElementById('loadingOverlay').style.display = 'none';
    
            // Redirige a estadoRegistro.html con estado de error
            window.location.href = 'estadoRegistro.html?status=error';
        });
    });
    

        // Alumno submit

        const alumnoForm = document.getElementById('participantForm');

        alumnoForm.addEventListener('submit', function (event) {
            event.preventDefault();

            // Mostrar el overlay de carga
            document.getElementById('loadingOverlay').style.display = 'flex';

            // Captura los datos del formulario
            const data = {
                nombreCompleto: document.getElementById('fullName').value || "",
                foto: document.getElementById('photo').files[0] ? document.getElementById('photo').files[0].name : "iconDefault.jpg",
                codigoGrupo: document.getElementById('groupCode').value || "",
                correoEducativo: document.getElementById('email').value || "",
                clave: document.getElementById('password').value || ""
            };

            // Realiza el fetch para enviar los datos
            fetch('http://localhost:8080/api/alumnos/alumno', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify(data)
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error('Error en la solicitud de registro de alumno');
                }
                return response.json();
            })
            .then(result => {
                console.log('Registro de alumno exitoso:', result);
                
                // Si el servidor devuelve un token de verificación, almacenarlo en localStorage
                if (result.verificationToken) {
                    localStorage.setItem('verificationToken', result.verificationToken);
                }

                // Oculta el overlay de carga
                document.getElementById('loadingOverlay').style.display = 'none';

                // Redirige a estadoRegistro.html con estado de éxito
                window.location.href = 'estadoRegistro.html?status=success';
            })
            .catch(error => {
                console.error('Error al registrar el alumno:', error);

                // Oculta el overlay de carga
                document.getElementById('loadingOverlay').style.display = 'none';

                // Redirige a estadoRegistro.html con estado de error
                window.location.href = 'estadoRegistro.html?status=error';
            });
        });



            //---------------------------------------------------------------------------------------------
    document.getElementById('loginForm').addEventListener('submit', function (e) {
        e.preventDefault(); // Para prevenir el envío directo del formulario
        document.getElementById('loadingOverlay').style.display = 'flex'; // Muestra el overlay
        // Aquí puedes agregar la lógica de autenticación, y luego ocultar el overlay
        setTimeout(() => {
            document.getElementById('loadingOverlay').style.display = 'none'; // Oculta el overlay después de la carga
        }, 3000); // Ejemplo de espera de 3 segundos antes de ocultar el overlay
    });

});
