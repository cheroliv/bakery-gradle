# Site Web Personnel - Cheroliv

Un site web moderne et professionnel pour un développeur formateur spécialisé en ingénierie pédagogique.

## 🚀 Fonctionnalités

### Système de Thèmes
- **Light Theme** : Thème clair classique
- **Dark Theme** : Thème sombre moderne
- **High Contrast** : Thème haute accessibilité
- Persistance des préférences via localStorage
- Détection automatique des préférences système
- Transitions fluides entre les thèmes
- Raccourci clavier : `Ctrl/Cmd + Shift + T`

### Design Moderne
- Interface responsive avec Bootstrap 5.3.8
- Animations CSS subtiles et performantes
- Cartes modernes avec effets de survol
- Typographie optimisée et accessible
- Icônes Bootstrap Icons 1.13.1

### Accessibilité
- Navigation au clavier complète
- Annonces vocales pour les changements de thème
- Lien de saut au contenu principal
- Contrastes respectant les standards WCAG
- Support des préférences de mouvement réduit

### Performance
- Lazy loading des images
- Preloading des ressources critiques
- Optimisation des animations
- Code JavaScript modulaire et organisé

## 📁 Structure du Projet

```
/
├── index.html          # Page principale
├── styles.css          # Styles CSS avec système de thèmes
├── script.js           # JavaScript vanilla pour l'interactivité
└── README.md          # Documentation du projet
```

## 🛠️ Technologies Utilisées

- **HTML5** : Structure sémantique
- **CSS3** : Styles modernes avec custom properties
- **JavaScript ES6+** : Interactivité et gestion des thèmes
- **Bootstrap 5.3.8** : Framework CSS responsive
- **Bootstrap Icons** : Icônes vectorielles

## 🎨 Système de Thèmes

Le système de thèmes utilise les CSS Custom Properties (variables CSS) pour une gestion centralisée des couleurs :

### Variables Principales
```css
:root {
  --bg-primary: #ffffff;
  --bg-secondary: #f8f9fa;
  --text-primary: #212529;
  --text-secondary: #6c757d;
  --accent-color: #0d6efd;
  /* ... autres variables */
}
```

### Thèmes Disponibles
- `light` : Thème par défaut
- `dark` : Thème sombre
- `high-contrast` : Thème haute accessibilité

## 🔧 Installation et Utilisation

### 1. Cloner le projet
```bash
git clone [URL_DU_REPO] # pas encore disponible
cd cheroliv-website
```

### 2. Lancer un serveur local
```bash
# Avec Python
python -m http.server 8000

# Avec Node.js
npx http-server

# Avec Java 18+
jwebserver -p 8000 -d ~/workspace/__repositories__/thymeleaf.cheroliv.com/__site__/doc/maquette 
```

### 3. Ouvrir dans le navigateur
```
http://localhost:8000
```

## 📱 Responsive Design

Le site est entièrement responsive avec des breakpoints optimisés :

- **Mobile** : < 576px
- **Tablette** : 576px - 768px
- **Desktop** : > 768px

## ⚡ Performance

### Optimisations Implementées
- Lazy loading des images
- Preloading des ressources critiques
- Transitions CSS optimisées
- JavaScript modulaire
- Compression des assets

### Métriques Cibles
- **Lighthouse Performance** : > 90
- **First Contentful Paint** : < 2s
- **Time to Interactive** : < 3s

## 🌐 Navigateurs Supportés

- Chrome 80+
- Firefox 75+
- Safari 13+
- Edge 80+

## 📝 Sections du Site

### 1. Accueil (Hero)
- Présentation principale
- Appels à l'action
- Animation d'entrée

### 2. À Propos
- Expertise professionnelle
- Statistiques clés
- Compétences techniques

### 3. Services
- Développement e-learning
- Conception pédagogique
- Formation et coaching

### 4. Expertise
- Technologies maîtrisées
- Méthodologies utilisées
- Domaines d'intervention

### 5. Portfolio
- Projets récents
- Technologies utilisées
- Études de cas

### 6. Formations
- Programmes disponibles
- Modalités d'apprentissage
- Certifications

### 7. Blog
- Articles récents
- Ressources pédagogiques
- Actualités du secteur

### 8. Contact
- Formulaire de contact
- Informations professionnelles
- Réseaux sociaux

## 🎯 Amélirations Futures

### Fonctionnalités Prévues
- [ ] Système de blog dynamique
- [ ] Formulaire de contact fonctionnel
- [ ] Intégration CMS headless
- [ ] PWA (Progressive Web App)
- [ ] Multilingue (FR/EN)

### Optimisations Techniques
- [ ] Service Worker pour le cache
- [ ] Optimisation des images (WebP)
- [ ] Bundle CSS/JS optimisé
- [ ] CDN pour les assets

## 📄 License

Ce projet est sous licence MIT. Voir le fichier `LICENSE` pour plus de détails.

## 👤 Auteur

**Cheroliv** - Développeur Formateur spécialisé en Ingénierie Pédagogique

- Site web: [https://cheroliv.com](https://cheroliv.com)
- LinkedIn: [Profil LinkedIn]
- GitHub: [Profil GitHub]

## 🤝 Contributions

Les contributions sont les bienvenues ! N'hésitez pas à :

1. Fork le projet
2. Créer une branche pour votre fonctionnalité
3. Commiter vos changements
4. Pusher vers la branche
5. Ouvrir une Pull Request

## 📞 Support

Pour toute question ou support, contactez-moi via :
- Email : contact@cheroliv.com
- LinkedIn : [Profil LinkedIn]
- GitHub Issues : [Lien vers les issues]

---

*Développé par @Cheroliv* 
