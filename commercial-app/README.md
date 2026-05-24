# 🛍️ CommercialApp — Spring Boot

Application commerciale complète avec gestion des rôles, produits, catégories et fournisseurs.

---

## 🏗️ Architecture du projet

```
commercial-app/
├── src/main/java/com/commercial/
│   ├── CommercialApplication.java          ← Point d'entrée
│   ├── config/
│   │   ├── SecurityConfig.java             ← Spring Security + routes
│   │   └── DataInitializer.java            ← Données de démarrage
│   ├── entity/
│   │   ├── Role.java                       ← Enum: SUPERADMIN, ADMIN, USER
│   │   ├── User.java                       ← Utilisateur (implémente UserDetails)
│   │   ├── Categorie.java                  ← Catégorie de produits
│   │   ├── Fournisseur.java                ← Fournisseur
│   │   └── Product.java                    ← Produit
│   ├── repository/
│   │   ├── UserRepository.java
│   │   ├── ProductRepository.java
│   │   ├── CategorieRepository.java
│   │   └── FournisseurRepository.java
│   ├── service/
│   │   ├── UserService.java                ← + UserDetailsService
│   │   ├── ProductService.java
│   │   ├── CategorieService.java
│   │   └── FournisseurService.java
│   └── controller/
│       ├── AuthController.java             ← /login, /register
│       ├── AdminController.java            ← /admin/**
│       ├── SuperAdminController.java       ← /superadmin/**
│       └── UserController.java             ← /user/**
│
└── src/main/resources/
    ├── application.properties
    └── templates/
        ├── fragments/layout.html           ← Sidebar, topbar, alerts
        ├── auth/
        │   ├── login.html
        │   └── register.html
        ├── admin/
        │   ├── dashboard.html
        │   ├── products/ (list.html, form.html)
        │   ├── categories/ (list.html, form.html)
        │   ├── fournisseurs/ (list.html, form.html)
        │   └── users/ (list.html)
        ├── superadmin/
        │   ├── dashboard.html
        │   ├── admins/ (list.html, form.html)
        │   └── users/ (list.html)
        └── user/
            ├── home.html
            ├── catalogue.html
            └── product-detail.html
```

---

## 👥 Rôles & Accès

| Rôle        | Dashboard         | Produits | Catégories | Fournisseurs | Gestion Admins | Gestion Users |
|-------------|-------------------|----------|------------|--------------|----------------|---------------|
| SUPERADMIN  | /superadmin/dashboard | ✅    | ✅         | ✅           | ✅ CRUD        | ✅ Tous       |
| ADMIN       | /admin/dashboard  | ✅ CRUD  | ✅ CRUD    | ✅ CRUD      | ❌             | ✅ View       |
| USER        | /user/home        | 👁️ View  | ❌         | ❌           | ❌             | ❌            |

---

## 🔐 Comptes de test (créés au démarrage)

| Rôle       | Email                  | Mot de passe  | Dashboard                    |
|------------|------------------------|---------------|------------------------------|
| SuperAdmin | superadmin@app.com     | superadmin123 | /superadmin/dashboard        |
| Admin      | admin@app.com          | admin123      | /admin/dashboard             |
| User       | user@app.com           | user123       | /user/home                   |

---

## 🗄️ Entités & Relations

```
User (id, nom, prenom, email, password, telephone, role, actif)
Categorie (id, nom, description, actif)
Fournisseur (id, nom, contact, email, telephone, adresse, matriculeFiscale, actif)
Product (id, nom, description, prix, prixAchat, stock, stockMin, reference, actif)
         ├── ManyToOne → Categorie
         └── ManyToOne → Fournisseur
```

---

## 🚀 Installation & Démarrage

### Prérequis
- Java 17+
- Maven 3.8+
- MySQL 8+ (ou utiliser H2 en dev)

### 1. Configurer la base de données

```sql
CREATE DATABASE commercial_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

### 2. Modifier `application.properties`

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/commercial_db?...
spring.datasource.username=root
spring.datasource.password=VotreMotDePasse
```

### 3. Lancer l'application

```bash
mvn spring-boot:run
```

L'application démarre sur **http://localhost:8080**

> ✅ Les données de test (users, catégories, fournisseurs, produits) sont automatiquement créées au démarrage.

---

## 🛣️ Routes principales

### 🔓 Public
| Route      | Description         |
|------------|---------------------|
| GET /      | Redirige vers login |
| GET /login | Page de connexion   |
| GET /register | Inscription      |

### 🔴 SuperAdmin (`/superadmin/**`)
| Route                        | Description               |
|------------------------------|---------------------------|
| GET /superadmin/dashboard    | Dashboard complet         |
| GET/POST /superadmin/admins  | Créer/lister admins       |
| GET /superadmin/admins/edit/{id} | Modifier admin        |
| GET /superadmin/admins/delete/{id} | Supprimer admin    |
| GET /superadmin/admins/toggle/{id} | Activer/désactiver |
| GET /superadmin/users        | Voir tous les utilisateurs|

### 🟠 Admin (`/admin/**`)
| Route                          | Description               |
|--------------------------------|---------------------------|
| GET /admin/dashboard           | Dashboard admin           |
| GET /admin/products            | Liste produits + search   |
| GET /admin/products/new        | Formulaire nouveau produit|
| POST /admin/products/save      | Sauvegarder produit       |
| GET /admin/products/edit/{id}  | Modifier produit          |
| POST /admin/products/update    | Mettre à jour produit     |
| GET /admin/products/delete/{id}| Supprimer produit         |
| GET /admin/products/toggle/{id}| Activer/désactiver produit|
| GET/POST /admin/categories     | Gérer catégories          |
| GET /admin/categories/edit/{id}| Modifier catégorie        |
| GET/POST /admin/fournisseurs   | Gérer fournisseurs        |
| GET /admin/users               | Voir les clients          |

### 🟢 User (`/user/**`)
| Route                  | Description             |
|------------------------|-------------------------|
| GET /user/home         | Page d'accueil catalogue|
| GET /user/products     | Catalogue filtrable     |
| GET /user/products/{id}| Détail produit          |

---

## 🛠️ Dépendances Maven

- `spring-boot-starter-web` — MVC
- `spring-boot-starter-thymeleaf` + `thymeleaf-extras-springsecurity6` — Templates
- `spring-boot-starter-security` — Authentification/autorisation
- `spring-boot-starter-data-jpa` — JPA/Hibernate
- `spring-boot-starter-validation` — Validation Bean
- `mysql-connector-j` — Pilote MySQL
- `lombok` — Réduction boilerplate
- `spring-boot-devtools` — Rechargement à chaud

---

## ✨ Fonctionnalités

- ✅ Authentification avec Spring Security
- ✅ 3 niveaux d'accès : SuperAdmin > Admin > User
- ✅ CRUD complet : Produits, Catégories, Fournisseurs, Users/Admins
- ✅ Alertes stock bas (produits sous le seuil minimum)
- ✅ Recherche sur produits, fournisseurs, utilisateurs
- ✅ Activation/désactivation des entités
- ✅ Dashboard avec statistiques en temps réel
- ✅ Interface utilisateur côté client (catalogue, filtres par catégorie)
- ✅ Données de test chargées automatiquement au démarrage
- ✅ Hashage des mots de passe (BCrypt)
