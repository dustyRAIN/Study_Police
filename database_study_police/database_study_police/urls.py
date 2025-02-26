"""database_study_police URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.0/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""


from django.conf.urls import url
from django.contrib import admin
from django.urls import include, path
from django.conf import settings
from django.conf.urls.static import static
from website import views

urlpatterns = [
    path('shouts/', include('notification.urls')),
    path('stats/', include('stats.urls')),
    path('face/', include('Face.urls')),
    path('classes/', include('classes.urls')),
    path('users/', include('users.urls')),
    path('materials/', include('materials.urls')),
    path('admin/', admin.site.urls),
    url(r'^rest-auth/', include('rest_auth.urls')),
    url(r'^accounts/', include('allauth.urls')),
    path('',views.Homepage, name='homepage'),
    path('class/<id>',views.Web_Class, name='Web_Class'),
    path('content/<id>',views.Class_materials, name='Class_materials'),
    path('signin/', views.SignIn, name='SignIn')

]

if settings.DEBUG:
    urlpatterns += static(settings.MEDIA_URL, document_root=settings.MEDIA_ROOT)
